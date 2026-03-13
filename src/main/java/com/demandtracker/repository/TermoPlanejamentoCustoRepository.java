package com.demandtracker.repository;

import com.demandtracker.entity.TermoPlanejamentoCusto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TermoPlanejamentoCustoRepository extends JpaRepository<TermoPlanejamentoCusto, Long> {

    @Modifying
    @Query("DELETE FROM TermoPlanejamentoCusto c WHERE c.termoPlanejamento.id = :termoPlanejamentoId")
    void deleteByTermoPlanejamentoId(@Param("termoPlanejamentoId") Long termoPlanejamentoId);
}
