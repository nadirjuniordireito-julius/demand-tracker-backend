package com.demandtracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoUpdateDTO {

    private Long usuarioId;
    private LocalDate dataInicioPlanejada;
    private LocalDate dataFimPlanejada;
    private LocalDate dataInicioReal;
    private LocalDate dataFimReal;
    private String status;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal percentualProgresso;
}
