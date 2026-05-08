package com.demandtracker.repository;

import com.demandtracker.entity.ProdutoSnapshotMensal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoSnapshotMensalRepository extends JpaRepository<ProdutoSnapshotMensal, Long> {

    Optional<ProdutoSnapshotMensal> findByMetaProdutoIdAndAnoAndMes(Long metaProdutoId, Integer ano, Integer mes);

    boolean existsByMetaProdutoIdAndAnoAndMes(Long metaProdutoId, Integer ano, Integer mes);

    Page<ProdutoSnapshotMensal> findByMetaProdutoId(Long metaProdutoId, Pageable pageable);

    Page<ProdutoSnapshotMensal> findByAno(Integer ano, Pageable pageable);

    Page<ProdutoSnapshotMensal> findByMes(Integer mes, Pageable pageable);

    Page<ProdutoSnapshotMensal> findByAnoAndMes(Integer ano, Integer mes, Pageable pageable);

    Page<ProdutoSnapshotMensal> findByMetaProdutoIdAndAno(Long metaProdutoId, Integer ano, Pageable pageable);

    Page<ProdutoSnapshotMensal> findByMetaProdutoIdAndAnoAndMes(Long metaProdutoId, Integer ano, Integer mes, Pageable pageable);

    @Query("SELECT s FROM ProdutoSnapshotMensal s "
            + "WHERE s.ano = :ano AND s.mes = :mes "
            + "AND (:projetoId IS NULL OR s.metaProduto.projetoMeta.projeto.id = :projetoId)")
    List<ProdutoSnapshotMensal> findRelatorioGestor(
            @Param("ano") Integer ano,
            @Param("mes") Integer mes,
            @Param("projetoId") Long projetoId
    );
}
