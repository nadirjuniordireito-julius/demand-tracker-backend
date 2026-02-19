package com.demandtracker.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * Configura o Jackson para aceitar "yyyy-MM-dd" em qualquer campo LocalDateTime
 * (interpretado como 00:00:00), além do formato ISO com hora.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeFlexDeserializer());
            builder.modulesToInstall(module);
        };
    }
}
