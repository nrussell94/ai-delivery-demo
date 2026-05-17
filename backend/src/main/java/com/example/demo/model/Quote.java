package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Document("quotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Quote {

    @Id
    private String id;

    private String driverName;

    private LocalDate dateOfBirth;

    private String licenceNumber;

    private String postcode;

    private String vehicleRef;

    private BigDecimal premium;

    @CreatedDate
    private Instant createdAt;
}
