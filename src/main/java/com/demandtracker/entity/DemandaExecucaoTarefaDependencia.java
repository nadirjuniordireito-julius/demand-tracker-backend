package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "demanda_execucao_tarefa_dependencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DemandaExecucaoTarefaDependencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_origem_id", nullable = false)
    private DemandaExecucaoTarefa tarefaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_destino_id", nullable = false)
    private DemandaExecucaoTarefa tarefaDestino;
}
