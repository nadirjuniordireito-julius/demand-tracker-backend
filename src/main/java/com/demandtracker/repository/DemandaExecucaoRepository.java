package com.demandtracker.repository;

import com.demandtracker.entity.DemandaExecucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DemandaExecucaoRepository extends JpaRepository<DemandaExecucao, Long> {

    Optional<DemandaExecucao> findByDemandaId(Long demandaTecnicaId);
}
