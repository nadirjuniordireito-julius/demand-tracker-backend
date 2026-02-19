package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoTotaisDTO {
    /** Soma de (quantidade * valorUnitario) de todos os MetaProduto das metas do projeto. */
    private BigDecimal valorTotalProjeto;
    /** Soma dos custos (qtdeHora * valorHora) de TermoEncerramentoCusto das demandas encerradas do projeto. */
    private BigDecimal valorTotalExecutado;
}
