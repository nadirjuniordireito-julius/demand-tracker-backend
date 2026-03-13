package com.demandtracker.dto;

import com.demandtracker.entity.DemandaExecucaoTarefaRecurso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoTarefaRecursoDTO {
    private Long id;
    private Long demandaExecucaoTarefaId;
    private Long profissionalId;
    private ProfissionalDTO profissional;
    private BigDecimal horasPlanejadas;

    public static DemandaExecucaoTarefaRecursoDTO fromEntity(DemandaExecucaoTarefaRecurso r) {
        if (r == null) return null;
        DemandaExecucaoTarefaRecursoDTO dto = new DemandaExecucaoTarefaRecursoDTO();
        dto.setId(r.getId());
        dto.setDemandaExecucaoTarefaId(r.getDemandaExecucaoTarefa() != null ? r.getDemandaExecucaoTarefa().getId() : null);
        dto.setProfissionalId(r.getProfissional() != null ? r.getProfissional().getId() : null);
        dto.setProfissional(r.getProfissional() != null ? ProfissionalDTO.fromEntity(r.getProfissional()) : null);
        dto.setHorasPlanejadas(r.getHorasPlanejadas());
        return dto;
    }
}
