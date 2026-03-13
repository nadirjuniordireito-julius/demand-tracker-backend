package com.demandtracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoTarefaRecursoCreateDTO {

    @NotNull(message = "ID da tarefa é obrigatório")
    private Long demandaExecucaoTarefaId;

    @NotNull(message = "ID do profissional é obrigatório")
    private Long profissionalId;

    @NotNull(message = "Horas planejadas é obrigatório")
    @DecimalMin(value = "0.00")
    private BigDecimal horasPlanejadas;
}
