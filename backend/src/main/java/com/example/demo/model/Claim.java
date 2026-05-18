package com.example.demo.model;

import com.example.demo.api.model.ClaimStatus;
import com.example.demo.api.model.IncidentType;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("claims")
public class Claim {

    @Id
    private String id;

    @Indexed(unique = true)
    private String claimReference;

    @Indexed
    private String policyId;

    private Instant incidentAt;

    private IncidentType incidentType;

    private String description;

    private String reportedBy;

    private ClaimStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Claim() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClaimReference() { return claimReference; }
    public void setClaimReference(String claimReference) { this.claimReference = claimReference; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public Instant getIncidentAt() { return incidentAt; }
    public void setIncidentAt(Instant incidentAt) { this.incidentAt = incidentAt; }

    public IncidentType getIncidentType() { return incidentType; }
    public void setIncidentType(IncidentType incidentType) { this.incidentType = incidentType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
