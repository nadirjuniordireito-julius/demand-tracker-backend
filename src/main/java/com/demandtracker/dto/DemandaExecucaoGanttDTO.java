package com.demandtracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO com dados da execução da demanda e tarefas no formato para o frontend montar o Gantt.
 * Recebe o ID da DemandaTecnica e retorna a execução (se existir) com as tarefas e dependências.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoGanttDTO {

    private Long demandaTecnicaId;
    private String demandaTecnicaCodigo;
    private String demandaTecnicaNome;
    private Long demandaExecucaoId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicioPlanejada;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFimPlanejada;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicioReal;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFimReal;

    private String status;
    private BigDecimal percentualProgresso;

    /** Tarefas da execução para o Gantt (com predecessores e recursos). */
    private List<DemandaExecucaoGanttTarefaDTO> tarefas;
}
