package com.demandtracker.repository;

import com.demandtracker.entity.TermoEncerramento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface TermoEncerramentoRepository extends JpaRepository<TermoEncerramento, Long> {
    Optional<TermoEncerramento> findByDemandaTecnicaId(Long demandaTecnicaId);
    
    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoEncerramentoCusto c")
    BigDecimal sumCustosRealizados();
}
