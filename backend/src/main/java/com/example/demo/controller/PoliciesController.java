package com.example.demo.controller;

import com.example.demo.api.PoliciesApi;
import com.example.demo.api.model.BindRequest;
import com.example.demo.api.model.Policy;
import com.example.demo.service.PolicyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PoliciesController implements PoliciesApi {

    private final PolicyService policyService;

    public PoliciesController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @Override
    public ResponseEntity<Policy> bindPolicy(BindRequest bindRequest) {
        PolicyService.BindResult result = policyService.bind(bindRequest.getQuoteId());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.policy());
    }

    @Override
    public ResponseEntity<Policy> getPolicy(String id) {
        Policy policy = policyService.get(id);
        return ResponseEntity.ok(policy);
    }
}
