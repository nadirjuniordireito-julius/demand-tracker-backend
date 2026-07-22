package com.demandtracker.repository;

import com.demandtracker.entity.DemandaExecucaoTarefaRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandaExecucaoTarefaRecursoRepository extends JpaRepository<DemandaExecucaoTarefaRecurso, Long> {

    List<DemandaExecucaoTarefaRecurso> findByDemandaExecucaoTarefaId(Long demandaExecucaoTarefaId);

    List<DemandaExecucaoTarefaRecurso> findByDemandaExecucaoTarefa_DemandaExecucaoId(Long demandaExecucaoId);

    List<DemandaExecucaoTarefaRecurso> findByProfissionalId(Long profissionalId);

    @Query("SELECT r FROM DemandaExecucaoTarefaRecurso r "
            + "JOIN FETCH r.demandaExecucaoTarefa t "
            + "JOIN FETCH t.demandaExecucao e "
            + "JOIN FETCH e.demanda d "
            + "WHERE r.profissional.id = :profissionalId")
    List<DemandaExecucaoTarefaRecurso> findByProfissionalIdWithDemandaTecnica(@Param("profissionalId") Long profissionalId);

    List<DemandaExecucaoTarefaRecurso> findByProfissionalIdAndDemandaExecucaoTarefa_DemandaExecucaoId(
            Long profissionalId, Long demandaExecucaoId);
}
