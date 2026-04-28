package com.demandtracker.repository;

import com.demandtracker.entity.DemandaExecucaoTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandaExecucaoTarefaRepository extends JpaRepository<DemandaExecucaoTarefa, Long> {

    List<DemandaExecucaoTarefa> findByDemandaExecucaoIdOrderBySequenciaAscIdAsc(Long demandaExecucaoId);
}
