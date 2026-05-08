package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSnapshotRelatorioGestorResumoDTO {

    private Integer totalProdutos;
    private Integer produtosVerde;
    private Integer produtosAmarelo;
    private Integer produtosVermelho;
    private Integer snapshotsFechados;
    private Integer snapshotsAbertos;
    private Integer totalAcoes;
    private Integer acoesAbertas;
    private Integer acoesEmAndamento;
    private Integer acoesConcluidas;
    private Integer acoesCanceladas;
    private Integer acoesVencidas;
    private Integer acoesImpactoAlto;
    private BigDecimal somaValorTotalExecutado;
    private BigDecimal somaValorTotalOrcamento;
    private BigDecimal percentualExecucaoConsolidado;
}
