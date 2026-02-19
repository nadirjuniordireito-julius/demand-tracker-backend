package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BubbleNodeDto {
    private Long id;
    /** Código da entidade quando aplicável (ex.: ProjetoMeta.codigo, MetaProduto.codigo, DemandaTecnica.codigo). */
    private String codigo;
    private String name;
    private String level;
    private BigDecimal value;
    private Double fillPercent;
    private String statusColor;
    private Double deviation;
    private Double impactoNoPai;
    private Double ist;
    private List<BubbleNodeDto> children;

    /** Quando level = "meta", "produto" ou "demanda": valor total previsto (no produto = valorUnitario * quantidade). */
    private BigDecimal valorTotalPrevisto;
    /** Quando level = "meta" ou "produto": valor total já executado = somatória dos custos realizados de todas as demandas encerradas (status G). No produto, exibir ao lado do valor previsto no header. */
    private BigDecimal valorTotalExecutado;
    /** Quando level = "meta", "produto" ou "demanda": percentual de execução = valorTotalExecutado / valorTotalPrevisto * 100. */
    private Double percentualExecucao;
    /** Quando level = "demanda": status da demanda (campo status da DemandaTecnica). */
    private String status;
    /** Quando level = "demanda": código da meta (ProjetoMeta) à qual o produto da demanda pertence. */
    private String codigoMeta;
    /** Quando level = "demanda": código do produto (MetaProduto) vinculado à demanda. */
    private String codigoProduto;
}
