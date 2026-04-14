package com.demandtracker.repository;

import com.demandtracker.entity.TermoPlanejamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TermoPlanejamentoRepository extends JpaRepository<TermoPlanejamento, Long> {
    Optional<TermoPlanejamento> findByDemandaTecnicaId(Long demandaTecnicaId);
    
    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoPlanejamentoCusto c")
    BigDecimal sumCustosPlanejados();

    /**
    * Soma o valor planejado (qtdeHora * valorHora) dos custos do termo de planejamento de uma demanda.
    */
    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoPlanejamentoCusto c " +
    "JOIN c.termoPlanejamento te WHERE te.demandaTecnica.id = :demandaId")
    BigDecimal sumValorPlanejadoyDemandaTecnicaId(@Param("demandaId") Long demandaId);

    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoPlanejamentoCusto c " +
            "JOIN c.termoPlanejamento te JOIN te.demandaTecnica d " +
            "WHERE d.metaProduto.id = :metaProdutoId")
    BigDecimal sumValorPlanejadoByMetaProdutoId(@Param("metaProdutoId") Long metaProdutoId);

    @Query("SELECT COALESCE(SUM(c.qtdeHora * c.valorHora), 0) FROM TermoPlanejamentoCusto c " +
            "JOIN c.termoPlanejamento te JOIN te.demandaTecnica d " +
            "WHERE d.metaProduto.id = :metaProdutoId " +
            "AND te.dataAbertura >= :dataInicio AND te.dataAbertura <= :dataFim")
    BigDecimal sumValorPlanejadoByMetaProdutoIdAndDataAberturaBetween(
            @Param("metaProdutoId") Long metaProdutoId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim);

    
}
