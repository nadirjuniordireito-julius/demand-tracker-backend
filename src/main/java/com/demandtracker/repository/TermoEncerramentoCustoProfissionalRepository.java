package com.demandtracker.repository;

import com.demandtracker.entity.TermoEncerramentoCustoProfissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TermoEncerramentoCustoProfissionalRepository 
        extends JpaRepository<TermoEncerramentoCustoProfissional, Long> {

    @Modifying
    @Query("DELETE FROM TermoEncerramentoCustoProfissional cp WHERE cp.termoEncerramentoCusto.termoEncerramento.id = :termoEncerramentoId")
    void deleteByTermoEncerramentoId(@Param("termoEncerramentoId") Long termoEncerramentoId);

    List<TermoEncerramentoCustoProfissional> 
        findByTermoEncerramentoCustoId(Long termoEncerramentoCustoId);

    /**
     * Soma total (qtdeHora * valorHora) por TermoEncerramentoCusto
     */
    @Query("""
           SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0)
           FROM TermoEncerramentoCustoProfissional c
           WHERE c.termoEncerramentoCusto.id = :termoEncerramentoCustoId
           """)
    BigDecimal sumTotalByTermoEncerramentoCustoId(
            @Param("termoEncerramentoCustoId") Long termoEncerramentoCustoId
    );

    /**
     * Soma total geral
     */
    @Query("""
           SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0)
           FROM TermoEncerramentoCustoProfissional c
           """)
    BigDecimal sumTotalGeral();
}
