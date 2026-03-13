package com.demandtracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoTarefaApontamentoProgressoUpdateDTO {

    private LocalDate data;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal percentual;

    @Size(max = 9000)
    private String comentario;
}
