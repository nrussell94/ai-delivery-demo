package com.example.demo.service;

import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final QuoteService quoteService;

    public PolicyService(PolicyRepository policyRepository, QuoteService quoteService) {
        this.policyRepository = policyRepository;
        this.quoteService = quoteService;
    }

    public com.example.demo.model.Policy bind(String quoteId) {
        var quote = quoteService.getQuote(quoteId);
        var effectiveFrom = Instant.now();
        var effectiveTo = effectiveFrom.plus(Duration.ofDays(365));
        var policy = new com.example.demo.model.Policy(
                UUID.randomUUID().toString(),
                quoteId,
                quote.getPremium(),
                effectiveFrom,
                effectiveTo,
                null
        );
        return policyRepository.save(policy);
    }

    public com.example.demo.model.Policy get(String policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found"));
    }
}
