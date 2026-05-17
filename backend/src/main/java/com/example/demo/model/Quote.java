package com.example.demo.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "quotes")
public class Quote {

    @Id
    private String id;
    private String name;
    private LocalDate dateOfBirth;
    private String driverLicenceNumber;
    private String postcode;
    private String vehicleRegistration;
    private BigDecimal premium;
    @CreatedDate
    private Instant createdAt;

    public Quote() {
    }

    public Quote(String id, String name, LocalDate dateOfBirth, String driverLicenceNumber,
                 String postcode, String vehicleRegistration, BigDecimal premium, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.driverLicenceNumber = driverLicenceNumber;
        this.postcode = postcode;
        this.vehicleRegistration = vehicleRegistration;
        this.premium = premium;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDriverLicenceNumber() {
        return driverLicenceNumber;
    }

    public void setDriverLicenceNumber(String driverLicenceNumber) {
        this.driverLicenceNumber = driverLicenceNumber;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
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
