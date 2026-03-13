package com.demandtracker.dto;

import com.demandtracker.entity.DemandaExecucao;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoDTO {
    private Long id;
    private Long demandaTecnicaId;
    private Long usuarioId;
    private UsuarioDTO usuario;
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCriacaoExecucao;
    private List<DemandaExecucaoTarefaDTO> tarefas;

    public static DemandaExecucaoDTO fromEntity(DemandaExecucao e) {
        if (e == null) return null;
        DemandaExecucaoDTO dto = new DemandaExecucaoDTO();
        dto.setId(e.getId());
        dto.setDemandaTecnicaId(e.getDemanda() != null ? e.getDemanda().getId() : null);
        dto.setUsuarioId(e.getUsuario() != null ? e.getUsuario().getId() : null);
        dto.setUsuario(e.getUsuario() != null ? UsuarioDTO.fromEntity(e.getUsuario()) : null);
        dto.setDataInicioPlanejada(e.getDataInicioPlanejada());
        dto.setDataFimPlanejada(e.getDataFimPlanejada());
        dto.setDataInicioReal(e.getDataInicioReal());
        dto.setDataFimReal(e.getDataFimReal());
        dto.setStatus(e.getStatus());
        dto.setPercentualProgresso(e.getPercentualProgresso());
        dto.setDataCriacaoExecucao(e.getDataCriacaoExecucao());
        dto.setTarefas(e.getTarefas() != null
                ? e.getTarefas().stream().map(DemandaExecucaoTarefaDTO::fromEntity).collect(Collectors.toList())
                : null);
        return dto;
    }
}
