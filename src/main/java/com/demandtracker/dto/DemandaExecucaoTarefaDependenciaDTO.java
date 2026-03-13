package com.demandtracker.dto;

import com.demandtracker.entity.DemandaExecucaoTarefaDependencia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoTarefaDependenciaDTO {
    private Long id;
    private Long tarefaOrigemId;
    private Long tarefaDestinoId;

    public static DemandaExecucaoTarefaDependenciaDTO fromEntity(DemandaExecucaoTarefaDependencia d) {
        if (d == null) return null;
        DemandaExecucaoTarefaDependenciaDTO dto = new DemandaExecucaoTarefaDependenciaDTO();
        dto.setId(d.getId());
        dto.setTarefaOrigemId(d.getTarefaOrigem() != null ? d.getTarefaOrigem().getId() : null);
        dto.setTarefaDestinoId(d.getTarefaDestino() != null ? d.getTarefaDestino().getId() : null);
        return dto;
    }
}
