package com.demandtracker.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Deserializa LocalDateTime aceitando tanto apenas data ("yyyy-MM-dd") quanto data e hora (ISO).
 * Quando só a data é enviada, usa 00:00:00 como hora.
 */
public class LocalDateTimeFlexDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText().trim();
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            if (value.length() <= 10) {
                return LocalDate.parse(value, DATE_ONLY).atStartOfDay();
            }
            return LocalDateTime.parse(value, DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value, DATE_ONLY).atStartOfDay();
            } catch (DateTimeParseException e2) {
                throw new IOException("Não foi possível interpretar data/hora: " + value, e);
            }
        }
    }
}
