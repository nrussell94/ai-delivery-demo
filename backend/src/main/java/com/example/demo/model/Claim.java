package com.example.demo.model;

import com.example.demo.api.model.ClaimStatus;
import com.example.demo.api.model.IncidentType;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("claims")
@Getter
@Setter
@NoArgsConstructor
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
}
