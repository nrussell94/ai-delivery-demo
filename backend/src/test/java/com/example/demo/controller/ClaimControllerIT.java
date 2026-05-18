package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.example.demo.api.model.IncidentType;
import com.example.demo.model.Claim;
import com.example.demo.repository.ClaimRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.data.mongodb.uri=mongodb://localhost:27018/test")
class ClaimControllerIT {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void resetState() {
        claimRepository.deleteAll();
    }

    @Test
    void postPersistsAndReturnsReference() throws Exception {
        OffsetDateTime incidentAt = OffsetDateTime.now(ZoneOffset.UTC).minusHours(2);
        Map<String, Object> body = Map.of(
            "policyId", "POL-1",
            "incidentAt", incidentAt.toString(),
            "incidentType", "COLLISION",
            "description", "Side mirror clipped by van",
            "reportedBy", "Sam Q"
        );

        MvcResult result = mockMvc.perform(post("/api/claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode response = MAPPER.readTree(result.getResponse().getContentAsString());
        String reference = response.get("claimReference").asText();
        assertThat(reference).matches("^FNOL-[A-Z2-9]{8}$");
        assertThat(response.get("status").asText()).isEqualTo("OPEN");

        Claim persisted = claimRepository.findAll().get(0);
        assertThat(persisted.getClaimReference()).isEqualTo(reference);
        assertThat(persisted.getPolicyId()).isEqualTo("POL-1");
        assertThat(persisted.getIncidentType()).isEqualTo(IncidentType.COLLISION);
        assertThat(persisted.getDescription()).isEqualTo("Side mirror clipped by van");
        assertThat(persisted.getReportedBy()).isEqualTo("Sam Q");
        assertThat(persisted.getCreatedAt()).isNotNull();
    }

    @Test
    void postRejectsOutOfWindow() throws Exception {
        OffsetDateTime future = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Map<String, Object> futureBody = Map.of(
            "policyId", "POL-2",
            "incidentAt", future.toString(),
            "incidentType", "THEFT",
            "description", "Caller reports vehicle stolen overnight",
            "reportedBy", "Alex P"
        );

        MvcResult futureResult = mockMvc.perform(post("/api/claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(futureBody)))
            .andExpect(status().isBadRequest())
            .andReturn();
        String futureMessage = MAPPER.readTree(futureResult.getResponse().getContentAsString())
            .get("message").asText().toLowerCase();
        assertThat(futureMessage).contains("future");

        OffsetDateTime tooOld = OffsetDateTime.now(ZoneOffset.UTC).minusDays(31);
        Map<String, Object> tooOldBody = Map.of(
            "policyId", "POL-2",
            "incidentAt", tooOld.toString(),
            "incidentType", "GLASS",
            "description", "Windscreen cracked by stone last month",
            "reportedBy", "Alex P"
        );

        MvcResult tooOldResult = mockMvc.perform(post("/api/claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(tooOldBody)))
            .andExpect(status().isBadRequest())
            .andReturn();
        String tooOldMessage = MAPPER.readTree(tooOldResult.getResponse().getContentAsString())
            .get("message").asText();
        assertThat(tooOldMessage).contains("30 days");

        assertThat(claimRepository.count()).isZero();
    }

    @Test
    void getReturnsNewestFirst() throws Exception {
        seedClaim("POL-A", "FNOL-AAAAAA01", Instant.parse("2026-05-18T07:00:00Z"));
        seedClaim("POL-A", "FNOL-AAAAAA02", Instant.parse("2026-05-18T08:00:00Z"));
        seedClaim("POL-A", "FNOL-AAAAAA03", Instant.parse("2026-05-18T08:30:00Z"));
        seedClaim("POL-B", "FNOL-BBBBBB01", Instant.parse("2026-05-18T09:00:00Z"));

        MvcResult result = mockMvc.perform(get("/api/claims").param("policyId", "POL-A"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode list = MAPPER.readTree(result.getResponse().getContentAsString());
        assertThat(list.isArray()).isTrue();
        assertThat(list.size()).isEqualTo(3);
        assertThat(list.get(0).get("claimReference").asText()).isEqualTo("FNOL-AAAAAA03");
        assertThat(list.get(1).get("claimReference").asText()).isEqualTo("FNOL-AAAAAA02");
        assertThat(list.get(2).get("claimReference").asText()).isEqualTo("FNOL-AAAAAA01");
    }

    private void seedClaim(String policyId, String reference, Instant createdAt) {
        Claim claim = new Claim();
        claim.setClaimReference(reference);
        claim.setPolicyId(policyId);
        claim.setIncidentAt(createdAt);
        claim.setIncidentType(IncidentType.COLLISION);
        claim.setDescription("seeded for ordering test");
        claim.setReportedBy("seeder");
        claim.setStatus(com.example.demo.api.model.ClaimStatus.OPEN);
        Claim saved = claimRepository.save(claim);

        mongoTemplate.getCollection("claims").updateOne(
            new Document("_id", new org.bson.types.ObjectId(saved.getId())),
            new Document("$set", new Document("createdAt", java.util.Date.from(createdAt)))
        );
    }

    private static org.springframework.test.web.servlet.result.StatusResultMatchers status() {
        return org.springframework.test.web.servlet.result.MockMvcResultMatchers.status();
    }
}
