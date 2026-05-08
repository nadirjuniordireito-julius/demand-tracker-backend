package com.demandtracker.dto;

import com.demandtracker.entity.enums.ImpactoAcao;
import com.demandtracker.entity.enums.TipoAcaoProduto;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Atualização parcial dos dados gerais da ação (sem mudar status).
 * Para evoluir o status use {@link ProdutoSnapshotAcaoUpdateStatusDTO}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSnapshotAcaoUpdateDTO {

    private TipoAcaoProduto tipoAcao;

    private String descricao;

    private Long responsavelId;

    @Size(max = 255, message = "Nome do responsável deve ter no máximo 255 caracteres")
    private String responsavelNome;

    private LocalDate prazo;

    private ImpactoAcao impacto;
}
