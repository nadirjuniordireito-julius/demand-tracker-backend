package com.demandtracker.repository;

import com.demandtracker.entity.TermoEncerramentoCusto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface TermoEncerramentoCustoRepository extends JpaRepository<TermoEncerramentoCusto, Long> {

    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoEncerramentoCusto c " +
           "WHERE c.termoEncerramento.demandaTecnica.metaProduto.id = :metaProdutoId")
    BigDecimal sumCustosByMetaProdutoId(@Param("metaProdutoId") Long metaProdutoId);

    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoEncerramentoCusto c " +
           "WHERE c.termoEncerramento.demandaTecnica.projeto.id = :projetoId")
    BigDecimal sumCustosByProjetoId(@Param("projetoId") Long projetoId);
}
