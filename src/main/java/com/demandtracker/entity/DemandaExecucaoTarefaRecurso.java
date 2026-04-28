package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "demanda_execucao_tarefa_recurso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DemandaExecucaoTarefaRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demanda_execucao_tarefa_id", nullable = false)
    private DemandaExecucaoTarefa demandaExecucaoTarefa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = true)
    private Perfil perfil;

    @Column(name = "horas_planejadas", precision = 10, scale = 2, nullable = false)
    private BigDecimal horasPlanejadas;

    @Column(name = "horas_executadas", precision = 10, scale = 2, nullable = true)
    private BigDecimal horasExecutadas;
}
