package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaAvaliacaoAnalyticsDTO {
    private List<AgregadoDTO> porProduto;
    private List<AgregadoDTO> porMeta;
    private List<AgregadoDTO> porProjeto;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgregadoDTO {
        private Long id;
        private String codigo;
        private String nome;
        private BigDecimal mediaIndiceSaude;
        private BigDecimal mediaIndiceConfiabilidade;
        private BigDecimal mediaIndiceRisco;
        private Long quantidadeAvaliacoes;
    }
}
