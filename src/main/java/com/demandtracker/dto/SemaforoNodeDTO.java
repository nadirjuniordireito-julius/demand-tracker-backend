package com.demandtracker.dto;

import com.demandtracker.entity.enums.SemaforoNivel;
import com.demandtracker.entity.enums.SemaforoStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Nó de semáforo em árvore (Projeto -> Metas -> Produtos).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemaforoNodeDTO {

    private Long id;
    private SemaforoNivel nivel;

    private String codigo;
    private String nome;

    private SemaforoStatus status;

    /** Código do status da demanda técnica (A,B,C,D,E,F,G,Z). Preenchido apenas quando nivel = DEMANDA. */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String statusDemanda;

    private LocalDate dataInicio;
    private LocalDate dataFim;

    private BigDecimal percentualExecutado;
    private Integer qtdDemandas;
    private Integer qtdDemandasEncerradas;

    /** Valor total previsto (meta = soma dos produtos; produto = valorUnitario * quantidade da MetaProduto). */
    private BigDecimal valorTotalPrevisto;
    /** Valor total executado (meta = soma dos produtos; produto = soma dos custos das demandas encerradas do produto). */
    private BigDecimal valorTotalExecutado;

    private List<SemaforoNodeDTO> children = new ArrayList<>();
}

