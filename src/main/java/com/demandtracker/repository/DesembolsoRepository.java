package com.demandtracker.repository;

import com.demandtracker.entity.Desembolso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface DesembolsoRepository extends JpaRepository<Desembolso, Long> {

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Desembolso p WHERE p.projeto.id = :projetoId")
    BigDecimal sumValorTotalDesembolsos(@Param("projetoId") Long projetoId);

    Page<Desembolso> findByProjetoId(Long projetoId, Pageable pageable);

    Page<Desembolso> findByProjetoIdAndDocumentoContainingIgnoreCase(Long projetoId, String documento, Pageable pageable);
}

