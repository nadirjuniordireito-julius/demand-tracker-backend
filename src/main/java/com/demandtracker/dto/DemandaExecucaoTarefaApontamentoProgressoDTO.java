package com.demandtracker.dto;

import com.demandtracker.entity.DemandaExecucaoTarefaApontamentoProgresso;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoTarefaApontamentoProgressoDTO {
    private Long id;
    private Long demandaExecucaoTarefaId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;
    private BigDecimal percentual;
    private String comentario;

    public static DemandaExecucaoTarefaApontamentoProgressoDTO fromEntity(DemandaExecucaoTarefaApontamentoProgresso a) {
        if (a == null) return null;
        DemandaExecucaoTarefaApontamentoProgressoDTO dto = new DemandaExecucaoTarefaApontamentoProgressoDTO();
        dto.setId(a.getId());
        dto.setDemandaExecucaoTarefaId(a.getDemandaExecucaoTarefa() != null ? a.getDemandaExecucaoTarefa().getId() : null);
        dto.setData(a.getData());
        dto.setPercentual(a.getPercentual());
        dto.setComentario(a.getComentario());
        return dto;
    }
}
