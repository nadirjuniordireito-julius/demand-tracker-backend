package com.demandtracker.dto;

import com.demandtracker.entity.TermoEncerramentoCusto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<TermoEncerramentoCustoProfissionalDTO> profissionais;

    public static TermoEncerramentoCustoDTO fromEntity(TermoEncerramentoCusto custo) {
        TermoEncerramentoCustoDTO dto = new TermoEncerramentoCustoDTO();
        dto.setId(custo.getId());
        dto.setTermoEncerramentoId(custo.getTermoEncerramento() != null
                ? custo.getTermoEncerramento().getId() : null);
        dto.setPerfilId(custo.getPerfil() != null ? custo.getPerfil().getId() : null);
        dto.setQtdeHora(custo.getQtdeHora());
        dto.setValorHora(custo.getValorHora());
        if (custo.getPerfil() != null) {
            dto.setPerfil(PerfilDTO.fromEntity(custo.getPerfil()));
        }
        if (custo.getProfissionais() != null && !custo.getProfissionais().isEmpty()) {
            dto.setProfissionais(custo.getProfissionais().stream()
                    .map(TermoEncerramentoCustoProfissionalDTO::fromEntity)
                    .collect(Collectors.toList()));
        } else {
            dto.setProfissionais(Collections.emptyList());
        }
        return dto;
    }
}
