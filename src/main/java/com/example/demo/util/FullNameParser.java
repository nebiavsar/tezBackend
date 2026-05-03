package com.example.demo.util;

import java.util.Arrays;

public final class FullNameParser {

    private FullNameParser() {
    }

    public static NameParts parse(String fullName) {
        String normalized = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("fullName is required.");
        }

        String[] parts = normalized.split(" ");
        if (parts.length == 1) {
            return new NameParts(parts[0], "");
        }

        String firstName = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
        String lastName = parts[parts.length - 1];
        return new NameParts(firstName, lastName);
    }

    public record NameParts(String firstName, String lastName) {
    }
}
