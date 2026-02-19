package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "demanda_avaliacao_texto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaAvaliacaoTexto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", nullable = false, unique = true)
    private DemandaAvaliacao avaliacao;

    @Column(name = "causa_atraso", columnDefinition = "TEXT")
    private String causaAtraso;

    @Column(name = "causa_custo", columnDefinition = "TEXT")
    private String causaCusto;

    @Column(name = "gargalo", columnDefinition = "TEXT")
    private String gargalo;

    @Column(name = "impacto_equipe", columnDefinition = "TEXT")
    private String impactoEquipe;

    @Column(name = "correcoes", columnDefinition = "TEXT")
    private String correcoes;

    @Column(name = "licoes_positivas", columnDefinition = "TEXT")
    private String licoesPositivas;

    @Column(name = "licoes_negativas", columnDefinition = "TEXT")
    private String licoesNegativas;

    @Column(name = "melhorias", columnDefinition = "TEXT")
    private String melhorias;
}
