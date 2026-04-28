package com.demandtracker.entity;

import com.demandtracker.entity.enums.StatusTarefa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "demanda_execucao_tarefa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DemandaExecucaoTarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demanda_execucao_id", nullable = false)
    private DemandaExecucao demandaExecucao;

    @Column(nullable = false, length = 500)
    private String titulo;

    @Column(nullable = true, length = 9000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusTarefa status;

    @Column(nullable = false, length = 1)
    private String prioridade;

    @Column(name = "data_inicio_planejada", nullable = false)
    private LocalDate dataInicioPlanejada;

    @Column(name = "data_fim_planejada", nullable = false)
    private LocalDate dataFimPlanejada;

    @Column(name = "data_inicio_real", nullable = true)
    private LocalDate dataInicioReal;

    @Column(name = "data_fim_real", nullable = true)
    private LocalDate dataFimReal;

    @Column(name = "percentual_progresso", precision = 10, scale = 2, nullable = false)
    private BigDecimal percentualProgresso;

    @Column(name = "estimativa_horas", precision = 10, scale = 2, nullable = false)
    private BigDecimal estimativaHoras;

    @Column(name = "sequencia")
    private Integer sequencia;

    @OneToMany(mappedBy = "demandaExecucaoTarefa", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<DemandaExecucaoTarefaRecurso> recursos;

    @OneToMany(mappedBy = "demandaExecucaoTarefa", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<DemandaExecucaoTarefaApontamentoProgresso> apontamentos;
}
