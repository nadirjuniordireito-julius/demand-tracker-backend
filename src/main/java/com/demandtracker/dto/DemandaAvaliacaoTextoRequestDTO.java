package com.demandtracker.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaAvaliacaoTextoRequestDTO {
    @JsonAlias("causa_atraso")
    private String causaAtraso;
    @JsonAlias("causa_custo")
    private String causaCusto;
    private String gargalo;
    @JsonAlias("impacto_equipe")
    private String impactoEquipe;
    private String correcoes;
    @JsonAlias("licoes_positivas")
    private String licoesPositivas;
    @JsonAlias("licoes_negativas")
    private String licoesNegativas;
    private String melhorias;
}
