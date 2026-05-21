package com.demandtracker.repository;

import com.demandtracker.entity.DemandaExecucaoTarefaRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandaExecucaoTarefaRecursoRepository extends JpaRepository<DemandaExecucaoTarefaRecurso, Long> {

    List<DemandaExecucaoTarefaRecurso> findByDemandaExecucaoTarefaId(Long demandaExecucaoTarefaId);

    List<DemandaExecucaoTarefaRecurso> findByDemandaExecucaoTarefa_DemandaExecucaoId(Long demandaExecucaoId);

    List<DemandaExecucaoTarefaRecurso> findByProfissionalId(Long profissionalId);

    List<DemandaExecucaoTarefaRecurso> findByProfissionalIdAndDemandaExecucaoTarefa_DemandaExecucaoId(
            Long profissionalId, Long demandaExecucaoId);
}
