package com.example.demo.service;

import com.example.demo.exception.EligibilityException;
import com.example.demo.repository.SupportedPostcodeRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

@Service
public class EligibilityService {

    private final SupportedPostcodeRepository supportedPostcodeRepository;
    private final Clock clock;

    public EligibilityService(SupportedPostcodeRepository supportedPostcodeRepository, Clock clock) {
        this.supportedPostcodeRepository = supportedPostcodeRepository;
        this.clock = clock;
    }

    public void check(LocalDate dateOfBirth, String postcode) {
        LocalDate today = LocalDate.now(clock);
        if (dateOfBirth.isAfter(today)) {
            throw new EligibilityException("Date of birth must be in the past");
        }

        int age = Period.between(dateOfBirth, today).getYears();
        if (age < 21) {
            throw new EligibilityException("Driver must be at least 21 years old");
        }

        String outward = extractOutward(postcode);
        if (!supportedPostcodeRepository.existsById(outward)) {
            throw new EligibilityException("Postcode not supported: " + outward);
        }
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
