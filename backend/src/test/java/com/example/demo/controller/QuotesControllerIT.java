package com.example.demo.controller;

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

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.data.mongodb.uri=mongodb://localhost:27018/test")
class QuotesControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired QuoteRepository quoteRepository;

    @BeforeEach
    void resetDb() { quoteRepository.deleteAll(); }

    @Test
    void ratesEligibleDriverByAgeAndPostcode() throws Exception {
        // (a) eligible 30yo SE15 → 500.00
        // Use LocalDate.now().minusYears(30).minusMonths(1) so the age computation lands on 30 regardless of the test day.
        var req30 = Map.of(
            "name", "Alex Driver",
            "dateOfBirth", LocalDate.now().minusYears(30).minusMonths(1).toString(),
            "driverLicenceNumber", "AB1234567",
            "postcode", "SE15",
            "vehicleRegistration", "AB12 CDE"
        );
        MvcResult r1 = mockMvc.perform(post("/api/quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req30)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quoteId").isNotEmpty())
            .andReturn();
        JsonNode body1 = objectMapper.readTree(r1.getResponse().getContentAsString());
        assertThat(body1.get("premium").asDouble()).isEqualTo(500.00);

        // (b) eligible 22yo "e1 7aa" (mixed case + inner space) → 500 * 1.4 * 1.1 = 770.00
        var req22 = Map.of(
            "name", "Sam Driver",
            "dateOfBirth", LocalDate.now().minusYears(22).minusMonths(2).toString(),
            "driverLicenceNumber", "CD1234567",
            "postcode", "e1 7aa",
            "vehicleRegistration", "CD12 XYZ"
        );
        MvcResult r2 = mockMvc.perform(post("/api/quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req22)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode body2 = objectMapper.readTree(r2.getResponse().getContentAsString());
        assertThat(body2.get("premium").asDouble()).isEqualTo(770.00);
        assertThat(body2.get("quoteId").asText()).isNotBlank();

        assertThat(quoteRepository.count()).isEqualTo(2);
    }

    @Test
    void rejectsIneligibleInputs() throws Exception {
        long before = quoteRepository.count();

        // (a) age 19 → 400
        var young = Map.of(
            "name", "Young Driver",
            "dateOfBirth", LocalDate.now().minusYears(19).toString(),
            "driverLicenceNumber", "AB1234567",
            "postcode", "E1",
            "vehicleRegistration", "AB12 CDE"
        );
        mockMvc.perform(post("/api/quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(young)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").isNotEmpty());

        // (b) postcode "M1" → 400
        var badPostcode = Map.of(
            "name", "Out Of Area",
            "dateOfBirth", LocalDate.now().minusYears(30).toString(),
            "driverLicenceNumber", "CD1234567",
            "postcode", "M1",
            "vehicleRegistration", "CD12 XYZ"
        );
        mockMvc.perform(post("/api/quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badPostcode)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").isNotEmpty());

        assertThat(quoteRepository.count()).isEqualTo(before);
    }
}
