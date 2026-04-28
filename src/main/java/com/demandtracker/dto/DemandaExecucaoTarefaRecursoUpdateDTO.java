package com.demandtracker.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoTarefaRecursoUpdateDTO {

    private Long profissionalId;
    private Long perfilId;

    @DecimalMin(value = "0.00")
    private BigDecimal horasPlanejadas;

    @DecimalMin(value = "0.00")
    private BigDecimal horasExecutadas;
}
