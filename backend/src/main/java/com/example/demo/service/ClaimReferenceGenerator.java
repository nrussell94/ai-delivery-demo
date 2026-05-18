package com.example.demo.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class ClaimReferenceGenerator {

    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int REFERENCE_LENGTH = 8;
    private static final String PREFIX = "FNOL-";

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(PREFIX.length() + REFERENCE_LENGTH);
        sb.append(PREFIX);
        for (int i = 0; i < REFERENCE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
