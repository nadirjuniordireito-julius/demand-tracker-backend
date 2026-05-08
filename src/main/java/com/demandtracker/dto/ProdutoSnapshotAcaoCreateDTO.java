package com.demandtracker.dto;

import com.demandtracker.entity.enums.ImpactoAcao;
import com.demandtracker.entity.enums.StatusAcaoProduto;
import com.demandtracker.entity.enums.TipoAcaoProduto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSnapshotAcaoCreateDTO {

    @NotNull(message = "Tipo da ação é obrigatório (PREVENTIVA/CORRETIVA/CONTINGENCIA)")
    private TipoAcaoProduto tipoAcao;

    @NotBlank(message = "Descrição da ação é obrigatória")
    private String descricao;

    private Long responsavelId;

    @Size(max = 255, message = "Nome do responsável deve ter no máximo 255 caracteres")
    private String responsavelNome;

    @NotNull(message = "Prazo é obrigatório")
    private LocalDate prazo;

    @NotNull(message = "Impacto é obrigatório (B/M/A)")
    private ImpactoAcao impacto;

    /**
     * Status inicial. Quando omitido o backend assume {@link StatusAcaoProduto#ABERTA}.
     */
    private StatusAcaoProduto statusAcao;

    private String observacaoStatus;
}
