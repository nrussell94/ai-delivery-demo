package com.example.demo;

import com.example.demo.api.model.QuoteRequest;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.data.mongodb.uri=mongodb://localhost:27018/test")
class QuoteEligibilityIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @BeforeEach
    void setUp() {
        quoteRepository.deleteAll();
        policyRepository.deleteAll();
    }

    @Test
    void underage_returns400() throws Exception {
        QuoteRequest request = new QuoteRequest(
                "Young Driver",
                LocalDate.now().minusYears(20),
                "DRIVE701054AB9XY",
                "E1",
                "AB12 CDE"
        );

        MvcResult result = mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String message = body.path("message").asText().toLowerCase();
        assertThat(message).satisfiesAnyOf(
                m -> assertThat(m).contains("21"),
                m -> assertThat(m).contains("age")
        );
    }

    @Test
    void unsupportedPostcode_returns400() throws Exception {
        QuoteRequest request = new QuoteRequest(
                "Adult Driver",
                LocalDate.now().minusYears(30),
                "DRIVE701054AB9XY",
                "W1",
                "AB12 CDE"
        );

        MvcResult result = mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String message = body.path("message").asText().toLowerCase();
        assertThat(message).contains("postcode");
    }
}
