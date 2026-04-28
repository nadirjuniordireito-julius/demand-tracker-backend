package com.demandtracker.dto;

import com.demandtracker.entity.enums.StatusTarefa;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Tarefa no formato para montagem do Gantt no frontend.
 * predecessorIds = IDs das tarefas que precisam ser concluídas antes desta (tarefa origem na dependência).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoGanttTarefaDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private StatusTarefa status;
    private String prioridade;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicioPlanejada;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFimPlanejada;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicioReal;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFimReal;

    private BigDecimal percentualProgresso;
    private BigDecimal estimativaHoras;
    private Integer sequencia;

    /** IDs das tarefas predecessoras (esta tarefa depende delas). */
    private List<Long> predecessorIds;

    /** Recursos (profissionais) alocados à tarefa. */
    private List<DemandaExecucaoGanttRecursoDTO> recursos;
}
