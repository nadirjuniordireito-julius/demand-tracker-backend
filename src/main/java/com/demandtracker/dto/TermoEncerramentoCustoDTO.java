package com.demandtracker.dto;

import com.demandtracker.entity.TermoEncerramentoCusto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoEncerramentoCustoDTO {
    private Long id;
    private Long termoEncerramentoId;
    private Long perfilId;
    private BigDecimal qtdeHora;
    private BigDecimal valorHora;
    private PerfilDTO perfil;
    
    public static TermoEncerramentoCustoDTO fromEntity(TermoEncerramentoCusto custo) {
        TermoEncerramentoCustoDTO dto = new TermoEncerramentoCustoDTO();
        dto.setId(custo.getId());
        dto.setTermoEncerramentoId(custo.getTermoEncerramento().getId());
        dto.setPerfilId(custo.getPerfil().getId());
        dto.setQtdeHora(custo.getQtdeHora());
        dto.setValorHora(custo.getValorHora());
        if (custo.getPerfil() != null) {
            dto.setPerfil(PerfilDTO.fromEntity(custo.getPerfil()));
        }
        return dto;
    }
}
