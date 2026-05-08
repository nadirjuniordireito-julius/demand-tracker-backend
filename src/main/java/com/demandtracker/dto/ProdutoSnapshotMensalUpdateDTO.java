package com.demandtracker.dto;

import com.demandtracker.entity.enums.StatusProdutoMes;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Atualização parcial de snapshot mensal (somente enquanto não fechado).
 * O par {@code ano/mes} e o {@code metaProdutoId} não são editáveis após criação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoSnapshotMensalUpdateDTO {

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
