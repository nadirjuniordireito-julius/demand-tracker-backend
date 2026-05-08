package com.demandtracker.dto;

import com.demandtracker.entity.enums.StatusProdutoMes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSnapshotRelatorioGestorItemDTO {

    private Long snapshotId;
    private Long metaProdutoId;
    private String codigoProduto;
    private String nomeProduto;
    private Long projetoMetaId;
    private String codigoMeta;
    private String nomeMeta;
    private Long projetoId;
    private String nomeProjeto;
    private Integer ano;
    private Integer mes;
    private StatusProdutoMes statusProdutoMes;
    private Boolean fechado;
    private BigDecimal percentualExecucao;
    private BigDecimal valorTotalOrcamento;
    private BigDecimal valorTotalEmExecucao;
    private BigDecimal valorTotalExecutado;
    private Integer totalAcoes;
    private Integer acoesAbertas;
    private Integer acoesEmAndamento;
    private Integer acoesConcluidas;
    private Integer acoesCanceladas;
    private Integer acoesVencidas;
    private Integer acoesImpactoAlto;
}
