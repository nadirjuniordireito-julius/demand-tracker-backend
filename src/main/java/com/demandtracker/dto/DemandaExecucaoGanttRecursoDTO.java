package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Recurso (profissional) alocado à tarefa, para exibição no Gantt.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoGanttRecursoDTO {
    private Long id;
    private Long profissionalId;
    private String nome;
    private Long perfilId;
    private String perfilNome;
    private BigDecimal horasPlanejadas;
    private BigDecimal horasExecutadas;
}
