package com.demandtracker.dto;

import com.demandtracker.entity.DemandaExecucaoTarefa;
import com.demandtracker.entity.enums.StatusTarefa;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoTarefaDTO {
    private Long id;
    private Long demandaExecucaoId;
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
    private List<DemandaExecucaoTarefaRecursoDTO> recursos;
    private List<DemandaExecucaoTarefaApontamentoProgressoDTO> apontamentos;

    public static DemandaExecucaoTarefaDTO fromEntity(DemandaExecucaoTarefa t) {
        if (t == null) return null;
        DemandaExecucaoTarefaDTO dto = new DemandaExecucaoTarefaDTO();
        dto.setId(t.getId());
        dto.setDemandaExecucaoId(t.getDemandaExecucao() != null ? t.getDemandaExecucao().getId() : null);
        dto.setTitulo(t.getTitulo());
        dto.setDescricao(t.getDescricao());
        dto.setStatus(t.getStatus());
        dto.setPrioridade(t.getPrioridade());
        dto.setDataInicioPlanejada(t.getDataInicioPlanejada());
        dto.setDataFimPlanejada(t.getDataFimPlanejada());
        dto.setDataInicioReal(t.getDataInicioReal());
        dto.setDataFimReal(t.getDataFimReal());
        dto.setPercentualProgresso(t.getPercentualProgresso());
        dto.setEstimativaHoras(t.getEstimativaHoras());
        dto.setSequencia(t.getSequencia());
        dto.setRecursos(t.getRecursos() != null
                ? t.getRecursos().stream().map(DemandaExecucaoTarefaRecursoDTO::fromEntity).collect(Collectors.toList())
                : null);
        dto.setApontamentos(t.getApontamentos() != null
                ? t.getApontamentos().stream().map(DemandaExecucaoTarefaApontamentoProgressoDTO::fromEntity).collect(Collectors.toList())
                : null);
        return dto;
    }
}
