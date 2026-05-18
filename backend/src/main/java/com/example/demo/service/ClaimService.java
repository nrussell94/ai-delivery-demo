package com.example.demo.service;

import com.example.demo.api.model.Claim;
import com.example.demo.api.model.ClaimRequest;
import com.example.demo.api.model.ClaimStatus;
import com.example.demo.repository.ClaimRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class ClaimService {

    static final int MAX_REFERENCE_RETRIES = 5;
    static final int MAX_DESCRIPTION_LENGTH = 2000;
    static final Duration MAX_INCIDENT_AGE = Duration.ofDays(30);

    private final ClaimRepository repository;
    private final ClaimReferenceGenerator referenceGenerator;
    private final Clock clock;

    public ClaimService(ClaimRepository repository, ClaimReferenceGenerator referenceGenerator, Clock clock) {
        this.repository = repository;
        this.referenceGenerator = referenceGenerator;
        this.clock = clock;
    }

    public Claim create(ClaimRequest request) {
        validate(request);

        DuplicateKeyException lastCollision = null;
        for (int attempt = 0; attempt < MAX_REFERENCE_RETRIES; attempt++) {
            com.example.demo.model.Claim persisted = new com.example.demo.model.Claim();
            persisted.setClaimReference(referenceGenerator.generate());
            persisted.setPolicyId(request.getPolicyId());
            persisted.setIncidentAt(request.getIncidentAt().toInstant());
            persisted.setIncidentType(request.getIncidentType());
            persisted.setDescription(request.getDescription().trim());
            persisted.setReportedBy(request.getReportedBy().trim());
            persisted.setStatus(ClaimStatus.OPEN);

            try {
                com.example.demo.model.Claim saved = repository.save(persisted);
                return toApi(saved);
            } catch (DuplicateKeyException collision) {
                lastCollision = collision;
            }
        }
        throw new IllegalStateException("Unable to generate a unique claim reference after " + MAX_REFERENCE_RETRIES + " attempts", lastCollision);
    }

    public List<Claim> listByPolicy(String policyId) {
        if (policyId == null || policyId.isBlank()) {
            throw new IllegalArgumentException("Policy id is required");
        }
        return repository.findByPolicyIdOrderByCreatedAtDesc(policyId).stream()
            .map(ClaimService::toApi)
            .toList();
    }

    private void validate(ClaimRequest request) {
        if (request.getPolicyId() == null || request.getPolicyId().isBlank()) {
            throw new IllegalArgumentException("Policy id is required");
        }
        if (request.getIncidentType() == null) {
            throw new IllegalArgumentException("Incident type is required");
        }
        if (request.getReportedBy() == null || request.getReportedBy().isBlank()) {
            throw new IllegalArgumentException("Reporter name is required");
        }
        validateDescription(request.getDescription());
        validateIncidentAt(request.getIncidentAt());
    }

    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
    }

    private void validateIncidentAt(OffsetDateTime incidentAt) {
        if (incidentAt == null) {
            throw new IllegalArgumentException("Incident date is required");
        }
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (incidentAt.isAfter(now)) {
            throw new IllegalArgumentException("Incident date cannot be in the future");
        }
        OffsetDateTime earliestAllowed = now.minus(MAX_INCIDENT_AGE);
        if (incidentAt.isBefore(earliestAllowed)) {
            throw new IllegalArgumentException("Incident date is more than 30 days ago — use the written claims process");
        }
    }

    private static Claim toApi(com.example.demo.model.Claim persisted) {
        Claim api = new Claim();
        api.setClaimReference(persisted.getClaimReference());
        api.setPolicyId(persisted.getPolicyId());
        api.setIncidentAt(persisted.getIncidentAt() != null
            ? persisted.getIncidentAt().atOffset(ZoneOffset.UTC)
            : null);
        api.setIncidentType(persisted.getIncidentType());
        api.setDescription(persisted.getDescription());
        api.setReportedBy(persisted.getReportedBy());
        api.setStatus(persisted.getStatus());
        if (persisted.getCreatedAt() != null) {
            api.setCreatedAt(persisted.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        if (persisted.getUpdatedAt() != null) {
            api.setUpdatedAt(persisted.getUpdatedAt().atOffset(ZoneOffset.UTC));
        }
        return api;
    }
}
