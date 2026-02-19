package com.demandtracker.repository;

import com.demandtracker.entity.TermoEncerramento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface TermoEncerramentoRepository extends JpaRepository<TermoEncerramento, Long> {
    Optional<TermoEncerramento> findByDemandaTecnicaId(Long demandaTecnicaId);

    /**
     * Soma o valor realizado (qtdeHora * valorHora) dos custos do termo de encerramento de uma demanda.
     */
    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoEncerramentoCusto c " +
           "JOIN c.termoEncerramento te WHERE te.demandaTecnica.id = :demandaId")
    BigDecimal sumValorExecutadoByDemandaTecnicaId(@Param("demandaId") Long demandaId);

    /**
     * Soma o valor realizado (qtdeHora * valorHora) de todos os custos dos termos de encerramento
     * das demandas encerradas (status G) do produto.
     */
    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoEncerramentoCusto c " +
           "JOIN c.termoEncerramento te JOIN te.demandaTecnica d " +
           "WHERE d.metaProduto.id = :metaProdutoId AND d.status = :status")
    BigDecimal sumValorExecutadoByMetaProdutoIdAndStatus(@Param("metaProdutoId") Long metaProdutoId, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoEncerramentoCusto c")
    BigDecimal sumCustosRealizados();
}
