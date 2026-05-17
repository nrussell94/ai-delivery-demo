package com.example.demo.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Document("quotes")
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

    public Quote() {
    }

    public Quote(String id, String driverName, LocalDate dateOfBirth, String licenceNumber,
                 String postcode, String vehicleRef, BigDecimal premium, Instant createdAt) {
        this.id = id;
        this.driverName = driverName;
        this.dateOfBirth = dateOfBirth;
        this.licenceNumber = licenceNumber;
        this.postcode = postcode;
        this.vehicleRef = vehicleRef;
        this.premium = premium;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getVehicleRef() {
        return vehicleRef;
    }

    public void setVehicleRef(String vehicleRef) {
        this.vehicleRef = vehicleRef;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
