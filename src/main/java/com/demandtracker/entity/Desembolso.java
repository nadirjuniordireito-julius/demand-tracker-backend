package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidade para registrar desembolsos financeiros.
 */
@Entity
@Table(name = "desembolso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Desembolso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Documento relacionado ao desembolso (opcional).
     */
    @Column(name = "documento", length = 500, nullable = true)
    private String documento;

    /**
     * Valor previsto do desembolso.
     * Not null, precision 10, scale 2.
     */
    @Column(name = "valor_previsto", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPrevisto;

    /**
     * Valor efetivo do desembolso.
     * Not null, precision 10, scale 2.
     */
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    /**
     * Data do desembolso.
     * Not null.
     */
    @Column(name = "data_desembolso", nullable = false)
    private LocalDate dataDesembolso;

    /**
     * Data prevista de desembolso.
     * Not null.
     */
    @Column(name = "data_prevista_desembolso", nullable = false)
    private LocalDate dataPrevistaDesembolso;

    /**
     * Relacionamento ManyToOne com Projeto.
     * Not null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;
}

