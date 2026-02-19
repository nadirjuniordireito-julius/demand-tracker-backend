package com.demandtracker.dto;

import com.demandtracker.entity.DemandaAvaliacaoTexto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaAvaliacaoTextoResponseDTO {
    private Long id;
    private String causaAtraso;
    private String causaCusto;
    private String gargalo;
    private String impactoEquipe;
    private String correcoes;
    private String licoesPositivas;
    private String licoesNegativas;
    private String melhorias;

    public static DemandaAvaliacaoTextoResponseDTO fromEntity(DemandaAvaliacaoTexto t) {
        if (t == null) return null;
        DemandaAvaliacaoTextoResponseDTO dto = new DemandaAvaliacaoTextoResponseDTO();
        dto.setId(t.getId());
        dto.setCausaAtraso(t.getCausaAtraso());
        dto.setCausaCusto(t.getCausaCusto());
        dto.setGargalo(t.getGargalo());
        dto.setImpactoEquipe(t.getImpactoEquipe());
        dto.setCorrecoes(t.getCorrecoes());
        dto.setLicoesPositivas(t.getLicoesPositivas());
        dto.setLicoesNegativas(t.getLicoesNegativas());
        dto.setMelhorias(t.getMelhorias());
        return dto;
    }
}
