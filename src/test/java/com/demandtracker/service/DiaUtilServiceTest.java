package com.demandtracker.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DiaUtilServiceTest {

    private final DiaUtilService service = new DiaUtilService();

    private static final Set<LocalDate> SEXTA_SANTA_2026 = Set.of(LocalDate.of(2026, 4, 3));

    @Test
    void tarefa5_comFeriado_alocaSaldoNoUltimoMes() {
        Map<YearMonth, BigDecimal> horas = service.calcularHorasPorMesPorDiasUteis(
                new BigDecimal("80"),
                LocalDate.of(2026, 3, 30),
                LocalDate.of(2026, 4, 10),
                SEXTA_SANTA_2026);

        assertThat(horas.get(YearMonth.of(2026, 3))).isEqualByComparingTo("16");
        assertThat(horas.get(YearMonth.of(2026, 4))).isEqualByComparingTo("64");
        assertThat(horas.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("80");
    }

    @Test
    void agregacaoQuatroTarefas_casoReal() {
        Map<YearMonth, BigDecimal> total = new HashMap<>();

        merge(total, service.calcularHorasPorMesPorDiasUteis(
                new BigDecimal("40"),
                LocalDate.of(2026, 3, 16),
                LocalDate.of(2026, 3, 20),
                SEXTA_SANTA_2026));
        merge(total, service.calcularHorasPorMesPorDiasUteis(
                new BigDecimal("40"),
                LocalDate.of(2026, 3, 23),
                LocalDate.of(2026, 3, 27),
                SEXTA_SANTA_2026));
        merge(total, service.calcularHorasPorMesPorDiasUteis(
                new BigDecimal("80"),
                LocalDate.of(2026, 3, 30),
                LocalDate.of(2026, 4, 10),
                SEXTA_SANTA_2026));
        merge(total, service.calcularHorasPorMesPorDiasUteis(
                new BigDecimal("160"),
                LocalDate.of(2026, 4, 13),
                LocalDate.of(2026, 5, 8),
                SEXTA_SANTA_2026));

        assertThat(total.get(YearMonth.of(2026, 3))).isEqualByComparingTo("96");
        assertThat(total.get(YearMonth.of(2026, 4))).isEqualByComparingTo("176");
        assertThat(total.get(YearMonth.of(2026, 5))).isEqualByComparingTo("48");
        assertThat(total.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("320");
    }

    @Test
    void quandoBrutasExcedemLimite_aplicaTetoSequencial() {
        Map<YearMonth, BigDecimal> horas = service.calcularHorasPorMesPorDiasUteis(
                new BigDecimal("50"),
                LocalDate.of(2026, 3, 30),
                LocalDate.of(2026, 4, 10),
                Set.of());

        assertThat(horas.get(YearMonth.of(2026, 3))).isEqualByComparingTo("16");
        assertThat(horas.get(YearMonth.of(2026, 4))).isEqualByComparingTo("34");
        assertThat(horas.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("50");
    }

    private static void merge(Map<YearMonth, BigDecimal> total, Map<YearMonth, BigDecimal> parcial) {
        parcial.forEach((ym, h) -> total.merge(ym, h, BigDecimal::add));
    }
}
