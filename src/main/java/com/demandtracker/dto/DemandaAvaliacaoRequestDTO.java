package com.demandtracker.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.demandtracker.entity.enums.Reutilizacao;
import com.demandtracker.entity.enums.TipoRisco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaAvaliacaoRequestDTO {
    /** Opcional: se não informada, será preenchida com a data do termo de encerramento da demanda. */
    private LocalDate dataEncerramento;
    @NotNull(message = "Atraso é obrigatório")
    private Boolean atraso;
    private BigDecimal desvioPrazoPercentual;
    /** Quando atraso=false, deve ser 0; quando atraso=true, entre 1 e 5. */
    @Min(value = 0, message = "Impacto atraso deve ser 0 (sem atraso) ou entre 1 e 5")
    @Max(value = 5, message = "Impacto atraso deve ser entre 0 e 5")
    private Integer impactoAtraso;
    private BigDecimal desvioCustoPercentual;
    @Min(value = 1, message = "Impacto financeiro deve ser entre 1 e 5")
    @Max(value = 5, message = "Impacto financeiro deve ser entre 1 e 5")
    private Integer impactoFinanceiro;
    @NotNull @Min(1) @Max(5)
    private Integer atendimentoRequisitos;
    @NotNull @Min(1) @Max(5)
    private Integer estabilidade;
    @NotNull @Min(0) @Max(3)
    private Integer retrabalho;
    @NotNull @Min(1) @Max(5)
    private Integer satisfacaoUsuario;
    @NotNull @Min(1) @Max(5)
    private Integer clarezaRequisitos;
    @NotNull @Min(1) @Max(5)
    private Integer qualidadePlanejamento;
    @NotNull @Min(1) @Max(5)
    private Integer aderenciaCronograma;
    @NotNull @Min(1) @Max(5)
    private Integer comunicacao;
    @NotNull @Min(1) @Max(5)
    private Integer capacidadeEquipe;
    @NotNull @Min(1) @Max(5)
    private Integer disponibilidadeEquipe;
    @NotNull
    private Boolean possuiBackupCritico;
    @NotNull
    private Boolean rotatividadeImpactou;
    @NotNull @Min(1) @Max(5)
    private Integer valorPercebido;
    @NotNull @Min(1) @Max(5)
    private Integer alinhamentoMeta;
    @NotNull
    private Reutilizacao reutilizacao;
    @NotNull @Min(1) @Max(5)
    private Integer avaliacaoGeral;
    @NotNull
    private Boolean repetiriaModelo;

    private List<TipoRisco> riscos;

    /** Aceita "texto" ou "textos" no JSON (front pode enviar textos). */
    @Valid
    @JsonAlias("textos")
    private DemandaAvaliacaoTextoRequestDTO texto;
}
