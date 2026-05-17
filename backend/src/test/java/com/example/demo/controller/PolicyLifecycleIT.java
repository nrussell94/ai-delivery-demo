package com.example.demo.controller;

import com.example.demo.repository.PolicyRepository;
import com.example.demo.repository.QuoteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.data.mongodb.uri=mongodb://localhost:27018/test")
class PolicyLifecycleIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired QuoteRepository quoteRepository;
    @Autowired PolicyRepository policyRepository;

    @BeforeEach
    void resetDb() {
        quoteRepository.deleteAll();
        policyRepository.deleteAll();
    }

    @Test
    void bindAndRetrieve() throws Exception {
        // 1) Create a quote.
        var quoteReq = Map.of(
            "name", "Lifecycle Driver",
            "dateOfBirth", LocalDate.now().minusYears(30).toString(),
            "driverLicenceNumber", "AB1234567",
            "postcode", "E5",
            "vehicleRegistration", "EE12 OOE"
        );
        Instant beforeBind = Instant.now();
        MvcResult quoteRes = mockMvc.perform(post("/api/quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(quoteReq)))
            .andExpect(status().isOk())
            .andReturn();
        String quoteId = objectMapper.readTree(quoteRes.getResponse().getContentAsString()).get("quoteId").asText();

        // 2) Bind it.
        MvcResult bindRes = mockMvc.perform(post("/api/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("quoteId", quoteId))))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode bind = objectMapper.readTree(bindRes.getResponse().getContentAsString());
        String policyId = bind.get("policyId").asText();
        assertThat(policyId).isNotBlank();
        assertThat(bind.get("quoteId").asText()).isEqualTo(quoteId);

        OffsetDateTime effectiveFrom = OffsetDateTime.parse(bind.get("effectiveFrom").asText());
        OffsetDateTime effectiveTo = OffsetDateTime.parse(bind.get("effectiveTo").asText());

        // effectiveFrom within ±5 min of test wall-clock
        Instant ef = effectiveFrom.toInstant();
        Instant afterBind = Instant.now();
        assertThat(ef).isAfterOrEqualTo(beforeBind.minus(Duration.ofMinutes(5)));
        assertThat(ef).isBeforeOrEqualTo(afterBind.plus(Duration.ofMinutes(5)));

        // 365-day span (allow ±1 second for clock drift inside the request)
        long deltaSeconds = Duration.between(ef, effectiveTo.toInstant()).getSeconds();
        long expected = Duration.ofDays(365).getSeconds();
        assertThat(deltaSeconds).isBetween(expected - 1, expected + 1);

        // 3) GET retrieves the same policy.
        MvcResult getRes = mockMvc.perform(get("/api/policies/" + policyId))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode got = objectMapper.readTree(getRes.getResponse().getContentAsString());
        assertThat(got.get("policyId").asText()).isEqualTo(policyId);
        assertThat(got.get("quoteId").asText()).isEqualTo(quoteId);
    }
}
