package com.demandtracker.dto;

import com.demandtracker.entity.TermoEncerramento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoEncerramentoDTO {
    private Long id;
    private Long demandaTecnicaId;
    private String resultadoEntregue;
    private LocalDateTime dataTermo;
    private Long usuarioId;
    private LocalDateTime dataAssinatura;
    private DemandaTecnicaDTO demandaTecnica;
    private UsuarioDTO usuario;
    private List<TermoEncerramentoCustoDTO> custos;
    
    public static TermoEncerramentoDTO fromEntity(TermoEncerramento termo) {
        TermoEncerramentoDTO dto = new TermoEncerramentoDTO();
        dto.setId(termo.getId());
        dto.setDemandaTecnicaId(termo.getDemandaTecnica().getId());
        dto.setResultadoEntregue(termo.getResultadoEntregue());
        dto.setDataTermo(termo.getDataTermo());
        dto.setUsuarioId(termo.getUsuario().getId());
        dto.setDataAssinatura(termo.getDataAssinatura());
        if (termo.getUsuario() != null) {
            dto.setUsuario(UsuarioDTO.fromEntity(termo.getUsuario()));
        }
        if (termo.getCustos() != null) {
            dto.setCustos(termo.getCustos().stream()
                .map(TermoEncerramentoCustoDTO::fromEntity)
                .collect(Collectors.toList()));
        }
        return dto;
    }
}
