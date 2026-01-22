package com.demandtracker.dto;

import com.demandtracker.entity.TermoPlanejamentoCusto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoPlanejamentoCustoDTO {
    private Long id;
    private Long termoPlanejamentoId;
    private Long perfilId;
    private BigDecimal qtdeHora;
    private BigDecimal valorHora;
    private PerfilDTO perfil;
    
    public static TermoPlanejamentoCustoDTO fromEntity(TermoPlanejamentoCusto custo) {
        TermoPlanejamentoCustoDTO dto = new TermoPlanejamentoCustoDTO();
        dto.setId(custo.getId());
        dto.setTermoPlanejamentoId(custo.getTermoPlanejamento().getId());
        dto.setPerfilId(custo.getPerfil().getId());
        dto.setQtdeHora(custo.getQtdeHora());
        dto.setValorHora(custo.getValorHora());
        if (custo.getPerfil() != null) {
            dto.setPerfil(PerfilDTO.fromEntity(custo.getPerfil()));
        }
        return dto;
    }
}
