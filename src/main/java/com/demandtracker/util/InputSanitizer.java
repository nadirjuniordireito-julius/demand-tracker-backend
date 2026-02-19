package com.demandtracker.util;

/**
 * Sanitização de inputs (uso interno em services).
 * Não altera contratos de API.
 */
public final class InputSanitizer {

    private InputSanitizer() {}

    /**
     * Retorna string trimada ou null se nula ou em branco.
     */
    public static String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    /**
     * Retorna string trimada e limitada ao máximo de caracteres.
     */
    public static String trimAndMaxLength(String value, int maxLength) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() <= maxLength) return trimmed;
        return trimmed.substring(0, maxLength);
    }
}
