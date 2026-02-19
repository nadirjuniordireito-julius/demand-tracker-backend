package com.demandtracker.repository;

import com.demandtracker.entity.DemandaAvaliacaoDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DemandaAvaliacaoDocRepository extends JpaRepository<DemandaAvaliacaoDoc, Long> {

    Optional<DemandaAvaliacaoDoc> findByAvaliacao_Id(Long avaliacaoId);

    boolean existsByAvaliacao_Id(Long avaliacaoId);

    void deleteByAvaliacao_Id(Long avaliacaoId);
}
