package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResumoDTO {
    private Long idMeta;
    private String codigoMeta;
    private String nomeMeta;
    private Long idProduto;
    private String codigoProduto;
    private String nomeProduto;
    private String situacao;
    private LocalDate inicioPrevisaoExecucao;
    private LocalDate inicioRealExecucao;
    private LocalDate fimPrevisaoExecucao;
    private Integer mesesPrevistosExecucao;
    private BigDecimal valorTotalOrcamento;
    private BigDecimal valorTotalEmExecucao;
    private BigDecimal valorTotalExecutado;
    private BigDecimal percentualExecucao;
    private BigDecimal valorMediaEntregaPrevistaMensal;
    private BigDecimal valorMediaEntregaRealMensal;
}
