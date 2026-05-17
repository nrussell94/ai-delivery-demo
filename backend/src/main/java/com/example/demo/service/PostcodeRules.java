package com.example.demo.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PostcodeRules {

    private static final Pattern DISTRICT_PATTERN =
            Pattern.compile("^(SE|E)(\\d+)([A-Z\\d]*)?$");

    private PostcodeRules() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.toUpperCase().replaceAll("\\s", "");
    }

    public static Optional<Integer> district(String raw) {
        String normalised = normalize(raw);
        Matcher matcher = DISTRICT_PATTERN.matcher(normalised);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(matcher.group(2)));
    }

    public static boolean isSupported(String raw) {
        String normalised = normalize(raw);
        Matcher matcher = DISTRICT_PATTERN.matcher(normalised);
        if (!matcher.matches()) {
            return false;
        }
        String prefix = matcher.group(1);
        int dist = Integer.parseInt(matcher.group(2));
        if ("E".equals(prefix)) {
            return dist >= 1 && dist <= 20;
        } else {
            // SE
            return dist >= 1 && dist <= 28;
        }
    }

    public static BigDecimal factor(String raw) {
        String normalised = normalize(raw);
        Matcher matcher = DISTRICT_PATTERN.matcher(normalised);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Postcode not supported: " + raw);
        }
        String prefix = matcher.group(1);
        int dist = Integer.parseInt(matcher.group(2));
        if ("E".equals(prefix)) {
            if (dist >= 1 && dist <= 20) {
                return new BigDecimal("1.1");
            }
        } else {
            // SE
            if (dist >= 1 && dist <= 28) {
                return new BigDecimal("1.0");
            }
        }
        throw new IllegalArgumentException("Postcode not supported: " + raw);
    }
}
