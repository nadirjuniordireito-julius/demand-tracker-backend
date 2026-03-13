package com.demandtracker.repository;

import com.demandtracker.entity.DemandaExecucaoTarefaDependencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandaExecucaoTarefaDependenciaRepository extends JpaRepository<DemandaExecucaoTarefaDependencia, Long> {

    List<DemandaExecucaoTarefaDependencia> findByTarefaOrigemId(Long tarefaOrigemId);
    List<DemandaExecucaoTarefaDependencia> findByTarefaDestinoId(Long tarefaDestinoId);

    @Modifying
    @Query("DELETE FROM DemandaExecucaoTarefaDependencia d WHERE d.tarefaOrigem.id = :tarefaId OR d.tarefaDestino.id = :tarefaId")
    void deleteByTarefaId(@Param("tarefaId") Long tarefaId);
}
