package com.example.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class RatingEngine {

    private static final BigDecimal BASE = new BigDecimal("500");

    private RatingEngine() {
    }

    public static BigDecimal ageFactor(int age) {
        if (age < 21) {
            throw new IllegalArgumentException("Age must be 21 or older");
        } else if (age <= 25) {
            return new BigDecimal("1.4");
        } else if (age <= 65) {
            return new BigDecimal("1.0");
        } else {
            return new BigDecimal("1.2");
        }
    }

    public static BigDecimal computePremium(int age, String postcode) {
        return BASE
                .multiply(ageFactor(age))
                .multiply(PostcodeRules.factor(postcode))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
