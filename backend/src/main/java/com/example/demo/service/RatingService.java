package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

@Service
public class RatingService {

    private final Clock clock;

    public RatingService(Clock clock) {
        this.clock = clock;
    }

    public BigDecimal price(LocalDate dateOfBirth, String postcode) {
        BigDecimal base = new BigDecimal("500");

        int age = Period.between(dateOfBirth, LocalDate.now(clock)).getYears();
        BigDecimal ageFactor;
        if (age <= 25) {
            ageFactor = new BigDecimal("1.4");
        } else if (age <= 65) {
            ageFactor = new BigDecimal("1.0");
        } else {
            ageFactor = new BigDecimal("1.2");
        }

        String outward = extractOutward(postcode);
        BigDecimal postcodeFactor;
        if (outward.startsWith("SE")) {
            postcodeFactor = new BigDecimal("1.0");
        } else if (outward.startsWith("E")) {
            postcodeFactor = new BigDecimal("1.1");
        } else {
            postcodeFactor = new BigDecimal("1.0");
        }

        return base.multiply(ageFactor).multiply(postcodeFactor).setScale(2, RoundingMode.HALF_UP);
    }

    private String extractOutward(String postcode) {
        String trimmed = postcode.trim().toUpperCase();
        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex >= 0) {
            return trimmed.substring(0, spaceIndex);
        }
        return trimmed;
    }
}
