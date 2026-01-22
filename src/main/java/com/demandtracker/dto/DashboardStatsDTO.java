package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Long totalDemandas;
    private Long demandasAbertas;
    private Long demandasEncerradas;
    private BigDecimal custosPlanejados;
    private BigDecimal custosRealizados;
}
