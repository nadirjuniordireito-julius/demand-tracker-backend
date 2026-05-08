package com.demandtracker.dto;

import com.demandtracker.entity.enums.StatusAcaoProduto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSnapshotAcaoUpdateStatusDTO {

    @NotNull(message = "Status da ação é obrigatório")
    private StatusAcaoProduto statusAcao;

    private String observacaoStatus;
}
