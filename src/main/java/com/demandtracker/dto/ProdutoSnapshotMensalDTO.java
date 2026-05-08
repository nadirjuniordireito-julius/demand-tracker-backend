package com.demandtracker.dto;

import com.demandtracker.entity.ProdutoSnapshotMensal;
import com.demandtracker.entity.enums.StatusProdutoMes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSnapshotMensalDTO {

    private Long id;
    private Long metaProdutoId;
    private MetaProdutoDTO metaProduto;
    private Integer ano;
    private Integer mes;
    private StatusProdutoMes statusProdutoMes;
    private String situacao;
    private BigDecimal valorTotalOrcamento;
    private BigDecimal valorTotalEmExecucao;
    private BigDecimal valorTotalExecutado;
    private BigDecimal percentualExecucao;
    private BigDecimal valorMediaEntregaPrevistaMensal;
    private BigDecimal valorMediaEntregaRealMensal;
    private String resumoAnalitico;
    private Boolean fechado;
    private LocalDateTime dataFechamento;
    private Long usuarioFechamentoId;
    private String usuarioFechamentoNome;
    private LocalDateTime dataRegistro;
    private LocalDateTime dataUpdate;

    public static ProdutoSnapshotMensalDTO fromEntity(ProdutoSnapshotMensal entity) {
        if (entity == null) {
            return null;
        }
        ProdutoSnapshotMensalDTO dto = new ProdutoSnapshotMensalDTO();
        dto.setId(entity.getId());
        dto.setAno(entity.getAno());
        dto.setMes(entity.getMes());
        dto.setStatusProdutoMes(entity.getStatusProdutoMes());
        dto.setSituacao(entity.getSituacao());
        dto.setValorTotalOrcamento(entity.getValorTotalOrcamento());
        dto.setValorTotalEmExecucao(entity.getValorTotalEmExecucao());
        dto.setValorTotalExecutado(entity.getValorTotalExecutado());
        dto.setPercentualExecucao(entity.getPercentualExecucao());
        dto.setValorMediaEntregaPrevistaMensal(entity.getValorMediaEntregaPrevistaMensal());
        dto.setValorMediaEntregaRealMensal(entity.getValorMediaEntregaRealMensal());
        dto.setResumoAnalitico(entity.getResumoAnalitico());
        dto.setFechado(entity.getFechado());
        dto.setDataFechamento(entity.getDataFechamento());
        dto.setDataRegistro(entity.getDataRegistro());
        dto.setDataUpdate(entity.getDataUpdate());
        if (entity.getMetaProduto() != null) {
            dto.setMetaProdutoId(entity.getMetaProduto().getId());
            dto.setMetaProduto(MetaProdutoDTO.fromEntity(entity.getMetaProduto()));
        }
        if (entity.getUsuarioFechamento() != null) {
            dto.setUsuarioFechamentoId(entity.getUsuarioFechamento().getId());
            dto.setUsuarioFechamentoNome(entity.getUsuarioFechamento().getNome());
        }
        return dto;
    }
}
