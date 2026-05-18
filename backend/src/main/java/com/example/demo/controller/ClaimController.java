package com.example.demo.controller;

import com.example.demo.api.ClaimsApi;
import com.example.demo.api.model.Claim;
import com.example.demo.api.model.ClaimRequest;
import com.example.demo.service.ClaimService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClaimController implements ClaimsApi {

    private final ClaimService service;

    public ClaimController(ClaimService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<Claim> createClaim(ClaimRequest request) {
        Claim created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    public ResponseEntity<List<Claim>> listClaims(String policyId) {
        return ResponseEntity.ok(service.listByPolicy(policyId));
    }
}
