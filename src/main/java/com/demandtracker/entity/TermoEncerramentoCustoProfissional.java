package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "termos_encerramento_custos_profissionais")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TermoEncerramentoCustoProfissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * FK para TermoEncerramentoCusto
     * Not null
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "termo_encerramento_custo_id", nullable = false)
    private TermoEncerramentoCusto termoEncerramentoCusto;

    /**
     * FK para Profissional
     * Not null
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    /**
     * Quantidade de horas
     * Not null, precision 10, scale 2
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal qtdeHora;

    /**
     * Valor hora aplicado no termo
     * Not null, precision 10, scale 2
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;
}
