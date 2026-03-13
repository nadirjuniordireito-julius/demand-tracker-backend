package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "demanda_execucao_tarefa_apontamento_progresso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DemandaExecucaoTarefaApontamentoProgresso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demanda_execucao_tarefa_id", nullable = false)
    private DemandaExecucaoTarefa demandaExecucaoTarefa;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "percentual", precision = 10, scale = 2, nullable = false)
    private BigDecimal percentual;

    @Column(nullable = true, length = 9000)
    private String comentario;
}
