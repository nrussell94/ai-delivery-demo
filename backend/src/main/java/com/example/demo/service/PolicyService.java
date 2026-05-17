package com.example.demo.service;

import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Quote;
import com.example.demo.repository.PolicyRepository;
import com.example.demo.repository.QuoteRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class PolicyService {

    private final QuoteRepository quoteRepository;
    private final PolicyRepository policyRepository;
    private final Clock clock;

    public PolicyService(QuoteRepository quoteRepository,
                         PolicyRepository policyRepository,
                         Clock clock) {
        this.quoteRepository = quoteRepository;
        this.policyRepository = policyRepository;
        this.clock = clock;
    }

    public BindResult bind(String quoteId) {
        Optional<com.example.demo.model.Policy> existing = policyRepository.findByQuoteId(quoteId);
        if (existing.isPresent()) {
            return new BindResult(toApiModel(existing.get()), false);
        }

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new NotFoundException("Quote not found: " + quoteId));

        LocalDate effectiveFrom = LocalDate.now(clock);
        LocalDate effectiveTo = effectiveFrom.plusDays(365);

        com.example.demo.model.Policy entity = new com.example.demo.model.Policy();
        entity.setQuoteId(quoteId);
        entity.setPremium(quote.getPremium());
        entity.setEffectiveFrom(effectiveFrom);
        entity.setEffectiveTo(effectiveTo);

        com.example.demo.model.Policy saved = policyRepository.save(entity);

        return new BindResult(toApiModel(saved), true);
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
        apiPolicy.setPremium(entity.getPremium());
        apiPolicy.setEffectiveFrom(entity.getEffectiveFrom());
        apiPolicy.setEffectiveTo(entity.getEffectiveTo());
        return apiPolicy;
    }

    public record BindResult(com.example.demo.api.model.Policy policy, boolean created) {
    }
}
