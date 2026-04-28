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
    private Long perfilId;
    private PerfilDTO perfil;
    private BigDecimal horasPlanejadas;
    private BigDecimal horasExecutadas;

    public static DemandaExecucaoTarefaRecursoDTO fromEntity(DemandaExecucaoTarefaRecurso r) {
        if (r == null) return null;
        DemandaExecucaoTarefaRecursoDTO dto = new DemandaExecucaoTarefaRecursoDTO();
        dto.setId(r.getId());
        dto.setDemandaExecucaoTarefaId(r.getDemandaExecucaoTarefa() != null ? r.getDemandaExecucaoTarefa().getId() : null);
        dto.setProfissionalId(r.getProfissional() != null ? r.getProfissional().getId() : null);
        dto.setProfissional(r.getProfissional() != null ? ProfissionalDTO.fromEntity(r.getProfissional()) : null);
        dto.setPerfilId(r.getPerfil() != null ? r.getPerfil().getId() : null);
        dto.setPerfil(r.getPerfil() != null ? PerfilDTO.fromEntity(r.getPerfil()) : null);
        dto.setHorasPlanejadas(r.getHorasPlanejadas());
        dto.setHorasExecutadas(r.getHorasExecutadas());
        return dto;
    }
}
