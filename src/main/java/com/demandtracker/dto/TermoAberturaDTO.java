package com.demandtracker.dto;

import com.demandtracker.entity.TermoAbertura;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoAberturaDTO {
    private Long id;
    private Long demandaTecnicaId;
    private String descricao;
    private LocalDateTime dataAbertura;
    private Long usuarioId;
    private LocalDateTime dataAssinatura;
    private DemandaTecnicaDTO demandaTecnica;
    private UsuarioDTO usuario;
    
    public static TermoAberturaDTO fromEntity(TermoAbertura termo) {
        TermoAberturaDTO dto = new TermoAberturaDTO();
        dto.setId(termo.getId());
        dto.setDemandaTecnicaId(termo.getDemandaTecnica().getId());
        dto.setDescricao(termo.getDescricao());
        dto.setDataAbertura(termo.getDataAbertura());
        dto.setUsuarioId(termo.getUsuario().getId());
        dto.setDataAssinatura(termo.getDataAssinatura());
        if (termo.getUsuario() != null) {
            dto.setUsuario(UsuarioDTO.fromEntity(termo.getUsuario()));
        }
        return dto;
    }
}
