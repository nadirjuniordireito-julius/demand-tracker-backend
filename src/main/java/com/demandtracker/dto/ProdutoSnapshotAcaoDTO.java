package com.demandtracker.dto;

import com.demandtracker.entity.ProdutoSnapshotAcao;
import com.demandtracker.entity.enums.ImpactoAcao;
import com.demandtracker.entity.enums.StatusAcaoProduto;
import com.demandtracker.entity.enums.TipoAcaoProduto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSnapshotAcaoDTO {

    private Long id;
    private Long snapshotId;
    private TipoAcaoProduto tipoAcao;
    private String descricao;
    private Long responsavelId;
    private String responsavelNome;
    private LocalDate prazo;
    private ImpactoAcao impacto;
    private StatusAcaoProduto statusAcao;
    private LocalDateTime dataStatus;
    private String observacaoStatus;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUpdate;

    public static ProdutoSnapshotAcaoDTO fromEntity(ProdutoSnapshotAcao entity) {
        if (entity == null) {
            return null;
        }
        ProdutoSnapshotAcaoDTO dto = new ProdutoSnapshotAcaoDTO();
        dto.setId(entity.getId());
        if (entity.getSnapshot() != null) {
            dto.setSnapshotId(entity.getSnapshot().getId());
        }
        dto.setTipoAcao(entity.getTipoAcao());
        dto.setDescricao(entity.getDescricao());
        if (entity.getResponsavel() != null) {
            dto.setResponsavelId(entity.getResponsavel().getId());
            if (entity.getResponsavelNome() == null || entity.getResponsavelNome().isBlank()) {
                dto.setResponsavelNome(entity.getResponsavel().getNome());
            } else {
                dto.setResponsavelNome(entity.getResponsavelNome());
            }
        } else {
            dto.setResponsavelNome(entity.getResponsavelNome());
        }
        dto.setPrazo(entity.getPrazo());
        dto.setImpacto(entity.getImpacto());
        dto.setStatusAcao(entity.getStatusAcao());
        dto.setDataStatus(entity.getDataStatus());
        dto.setObservacaoStatus(entity.getObservacaoStatus());
        dto.setDataCriacao(entity.getDataCriacao());
        dto.setDataUpdate(entity.getDataUpdate());
        return dto;
    }
}
