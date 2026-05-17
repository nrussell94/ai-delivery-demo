package com.example.demo;

import com.example.demo.api.model.BindRequest;
import com.example.demo.api.model.Policy;
import com.example.demo.api.model.QuoteRequest;
import com.example.demo.api.model.QuoteResponse;
import com.example.demo.repository.PolicyRepository;
import com.example.demo.repository.QuoteRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.data.mongodb.uri=mongodb://localhost:27018/test")
class QuoteAndBindIT {

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
    void quoteThenBindThenFetch_returnsPolicy() throws Exception {
        QuoteRequest quoteRequest = new QuoteRequest(
                "Alex Driver",
                LocalDate.now().minusYears(30),
                "DRIVE701054AB9XY",
                "E14 5AB",
                "AB12 CDE"
        );

        MvcResult quoteResult = mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quoteRequest)))
                .andExpect(status().isOk())
                .andReturn();

        QuoteResponse quoteResponse = objectMapper.readValue(
                quoteResult.getResponse().getContentAsString(), QuoteResponse.class);

        assertThat(quoteResponse.getPremium()).isEqualTo(550.0);
        String quoteId = quoteResponse.getQuoteId();

        BindRequest bindRequest = new BindRequest(quoteId);

        MvcResult bindResult = mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bindRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Policy policy = objectMapper.readValue(
                bindResult.getResponse().getContentAsString(), Policy.class);

        assertThat(policy.getPolicyId()).isNotNull();
        assertThat(policy.getQuoteId()).isEqualTo(quoteId);
        assertThat(policy.getPremium()).isEqualTo(550.0);
        assertThat(policy.getEffectiveFrom()).isEqualTo(LocalDate.now());
        assertThat(policy.getEffectiveTo()).isEqualTo(LocalDate.now().plusDays(365));

        String policyId = policy.getPolicyId();

        MvcResult getResult = mockMvc.perform(get("/api/policies/{id}", policyId))
                .andExpect(status().isOk())
                .andReturn();

        Policy fetched = objectMapper.readValue(
                getResult.getResponse().getContentAsString(), Policy.class);

        assertThat(fetched.getPolicyId()).isEqualTo(policyId);
    }
}
