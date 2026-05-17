package com.example.demo.service;

import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Quote;
import com.example.demo.repository.PolicyRepository;
import com.example.demo.repository.QuoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PolicyService {

    private final QuoteRepository quoteRepository;
    private final PolicyRepository policyRepository;

    public PolicyService(QuoteRepository quoteRepository, PolicyRepository policyRepository) {
        this.quoteRepository = quoteRepository;
        this.policyRepository = policyRepository;
    }

    public com.example.demo.api.model.Policy bind(String quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new NotFoundException("Quote not found: " + quoteId));

        LocalDate effectiveFrom = LocalDate.now();
        LocalDate effectiveTo = effectiveFrom.plusDays(365);

        com.example.demo.model.Policy entity = new com.example.demo.model.Policy();
        entity.setQuoteId(quoteId);
        entity.setPremium(quote.getPremium());
        entity.setEffectiveFrom(effectiveFrom);
        entity.setEffectiveTo(effectiveTo);

        com.example.demo.model.Policy saved = policyRepository.save(entity);

        return toApiModel(saved);
    }

    public com.example.demo.api.model.Policy get(String policyId) {
        com.example.demo.model.Policy entity = policyRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found: " + policyId));

        return toApiModel(entity);
    }

    private com.example.demo.api.model.Policy toApiModel(com.example.demo.model.Policy entity) {
        com.example.demo.api.model.Policy apiPolicy = new com.example.demo.api.model.Policy();
        apiPolicy.setPolicyId(entity.getId());
        apiPolicy.setQuoteId(entity.getQuoteId());
        apiPolicy.setPremium(entity.getPremium().doubleValue());
        apiPolicy.setEffectiveFrom(entity.getEffectiveFrom());
        apiPolicy.setEffectiveTo(entity.getEffectiveTo());
        return apiPolicy;
    }
}
