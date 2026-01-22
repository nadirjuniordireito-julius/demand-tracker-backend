package com.demandtracker.repository;

import com.demandtracker.entity.TermoPlanejamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface TermoPlanejamentoRepository extends JpaRepository<TermoPlanejamento, Long> {
    Optional<TermoPlanejamento> findByDemandaTecnicaId(Long demandaTecnicaId);
    
    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoPlanejamentoCusto c")
    BigDecimal sumCustosPlanejados();
}
