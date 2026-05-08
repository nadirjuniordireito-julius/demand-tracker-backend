package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Relatório gerencial mensal: consolidado + lista por produto + ações vencidas no período.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSnapshotRelatorioGestorDTO {

    private Integer ano;
    private Integer mes;
    private Long projetoId;
    private ProdutoSnapshotRelatorioGestorResumoDTO resumo;
    private List<ProdutoSnapshotRelatorioGestorItemDTO> produtos;
    private List<ProdutoSnapshotAcaoDTO> acoesVencidas;
    private List<ProdutoSnapshotRelatorioGestorItemDTO> produtosCriticos;
}
