package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "demanda_execucao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DemandaExecucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demanda_tecnica_id", nullable = false, unique = true)
    private DemandaTecnica demanda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;

    @Column(name = "data_inicio_planejada", nullable = false)
    private LocalDate dataInicioPlanejada;

    @Column(name = "data_fim_planejada", nullable = false)
    private LocalDate dataFimPlanejada;

    @Column(name = "data_inicio_real", nullable = true)
    private LocalDate dataInicioReal;

    @Column(name = "data_fim_real", nullable = true)
    private LocalDate dataFimReal;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "percentual_progresso", precision = 10, scale = 2, nullable = false)
    private BigDecimal percentualProgresso;

    @Column(name = "data_criacao_execucao", nullable = false)
    private LocalDateTime dataCriacaoExecucao;

    @OneToMany(mappedBy = "demandaExecucao", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<DemandaExecucaoTarefa> tarefas;

    @PrePersist
    protected void onCreate() {
        if (dataCriacaoExecucao == null) {
            dataCriacaoExecucao = LocalDateTime.now();
        }
    }
}
