package com.demandtracker.dto;

import com.demandtracker.entity.enums.StatusProdutoMes;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Cria um snapshot mensal por produto. Os valores numéricos são opcionais
 * porque, quando não informados, o backend pode capturá-los a partir do
 * resumo atual do produto ({@code ProdutoResumoDTO}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSnapshotMensalCreateDTO {

    @NotNull(message = "ID do produto (metaProdutoId) é obrigatório")
    private Long metaProdutoId;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900")
    @Max(value = 3000, message = "Ano deve ser menor ou igual a 3000")
    private Integer ano;

    @NotNull(message = "Mês é obrigatório")
    @Min(value = 1, message = "Mês deve ser entre 1 e 12")
    @Max(value = 12, message = "Mês deve ser entre 1 e 12")
    private Integer mes;

    @NotNull(message = "Status do produto no mês é obrigatório (V/A/R)")
    private StatusProdutoMes statusProdutoMes;

    @Size(max = 255, message = "Situação deve ter no máximo 255 caracteres")
    private String situacao;

    @DecimalMin(value = "0.00", message = "Valor total do orçamento deve ser >= 0")
    private BigDecimal valorTotalOrcamento;

    @DecimalMin(value = "0.00", message = "Valor total em execução deve ser >= 0")
    private BigDecimal valorTotalEmExecucao;

    @DecimalMin(value = "0.00", message = "Valor total executado deve ser >= 0")
    private BigDecimal valorTotalExecutado;

    @DecimalMin(value = "0.00", message = "Percentual de execução deve ser >= 0")
    private BigDecimal percentualExecucao;

    @DecimalMin(value = "0.00", message = "Valor médio previsto mensal deve ser >= 0")
    private BigDecimal valorMediaEntregaPrevistaMensal;

    @DecimalMin(value = "0.00", message = "Valor médio real mensal deve ser >= 0")
    private BigDecimal valorMediaEntregaRealMensal;

    private String resumoAnalitico;
}
