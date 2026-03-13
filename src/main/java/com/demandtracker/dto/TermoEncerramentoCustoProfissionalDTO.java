package com.demandtracker.dto;

import com.demandtracker.entity.TermoEncerramentoCustoProfissional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoEncerramentoCustoProfissionalDTO {
    private Long id;
    private Long termoEncerramentoId;
    private BigDecimal qtdeHora;
    private BigDecimal valorHora;
    private ProfissionalDTO profissional;
    
    public static TermoEncerramentoCustoProfissionalDTO fromEntity(TermoEncerramentoCustoProfissional custo) {
        TermoEncerramentoCustoProfissionalDTO dto = new TermoEncerramentoCustoProfissionalDTO();
        dto.setId(custo.getId());
        dto.setTermoEncerramentoId(custo.getTermoEncerramentoCusto() != null
                ? custo.getTermoEncerramentoCusto().getId() : null);
        dto.setQtdeHora(custo.getQtdeHora());
        dto.setValorHora(custo.getValorHora());
        if (custo.getProfissional() != null) {
            dto.setProfissional(ProfissionalDTO.fromEntity(custo.getProfissional()));
        }
        return dto;
    }
}
