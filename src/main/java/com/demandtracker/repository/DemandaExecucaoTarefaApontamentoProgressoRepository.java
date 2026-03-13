package com.demandtracker.repository;

import com.demandtracker.entity.DemandaExecucaoTarefaApontamentoProgresso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandaExecucaoTarefaApontamentoProgressoRepository extends JpaRepository<DemandaExecucaoTarefaApontamentoProgresso, Long> {

    List<DemandaExecucaoTarefaApontamentoProgresso> findByDemandaExecucaoTarefaIdOrderByDataAsc(Long demandaExecucaoTarefaId);
}
