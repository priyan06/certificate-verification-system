package com.example.certificateverification.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InitialPasswordGenerator {

    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    /**
     * Generates an initial password from the first 4 alphabetic letters of the student's name
     * concatenated with the Date of Birth in DDMMYYYY format.
     *
     * Example: "Priyan S", 2002-06-19 -> "Priy19062002"
     * Example: "A. Kumar", 2002-06-19 -> "AKum19062002"
     * Example: "Ram", 2003-01-01 -> "Ram01012003"
     */
    public static String generate(String studentName, LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth is required for initial password generation");
        }

        String prefix = "";
        if (studentName != null) {
            // Keep only letters (preserving case)
            String lettersOnly = studentName.replaceAll("[^a-zA-Z]", "");
            int take = Math.min(4, lettersOnly.length());
            prefix = lettersOnly.substring(0, take);
        }

        // Fallback prefix if name has no alphabetic characters
        if (prefix.isEmpty()) {
            prefix = "STU";
        }

        String dobPart = dateOfBirth.format(DOB_FORMATTER);
        return prefix + dobPart;
    }
}
