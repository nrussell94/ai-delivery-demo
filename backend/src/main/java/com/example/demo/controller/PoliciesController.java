package com.example.demo.controller;

import com.example.demo.api.PoliciesApi;
import com.example.demo.api.model.BindRequest;
import com.example.demo.service.PolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
public class PoliciesController implements PoliciesApi {

    private final PolicyService policyService;

    public PoliciesController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @Override
    public ResponseEntity<com.example.demo.api.model.Policy> bindPolicy(BindRequest bindRequest) {
        var policy = policyService.bind(bindRequest.getQuoteId());
        return ResponseEntity.ok(toApi(policy));
    }

    @Override
    public ResponseEntity<com.example.demo.api.model.Policy> getPolicy(String id) {
        var policy = policyService.get(id);
        return ResponseEntity.ok(toApi(policy));
    }

    private static com.example.demo.api.model.Policy toApi(com.example.demo.model.Policy p) {
        return new com.example.demo.api.model.Policy(
            p.getId(),
            p.getQuoteId(),
            p.getPremium().doubleValue(),
            OffsetDateTime.ofInstant(p.getEffectiveFrom(), ZoneOffset.UTC),
            OffsetDateTime.ofInstant(p.getEffectiveTo(), ZoneOffset.UTC)
        );
    }
}
