package com.demandtracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoCreateDTO {

    @NotNull(message = "ID da demanda técnica é obrigatório")
    private Long demandaTecnicaId;

    private Long usuarioId;

    @NotNull(message = "Data início planejada é obrigatória")
    private LocalDate dataInicioPlanejada;

    @NotNull(message = "Data fim planejada é obrigatória")
    private LocalDate dataFimPlanejada;

    private LocalDate dataInicioReal;
    private LocalDate dataFimReal;

    @NotNull(message = "Status é obrigatório")
    private String status;

    @NotNull(message = "Percentual de progresso é obrigatório")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal percentualProgresso;
}
