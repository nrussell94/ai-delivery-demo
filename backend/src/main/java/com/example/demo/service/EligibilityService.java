package com.example.demo.service;

import com.example.demo.exception.EligibilityException;
import com.example.demo.repository.SupportedPostcodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class EligibilityService {

    private final SupportedPostcodeRepository supportedPostcodeRepository;

    public EligibilityService(SupportedPostcodeRepository supportedPostcodeRepository) {
        this.supportedPostcodeRepository = supportedPostcodeRepository;
    }

    public void check(LocalDate dateOfBirth, String postcode) {
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
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
