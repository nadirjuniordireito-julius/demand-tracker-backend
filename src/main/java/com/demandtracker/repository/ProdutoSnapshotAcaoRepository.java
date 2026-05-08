package com.demandtracker.repository;

import com.demandtracker.entity.ProdutoSnapshotAcao;
import com.demandtracker.entity.enums.StatusAcaoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProdutoSnapshotAcaoRepository extends JpaRepository<ProdutoSnapshotAcao, Long> {

    List<ProdutoSnapshotAcao> findBySnapshotIdOrderByPrazoAsc(Long snapshotId);

    List<ProdutoSnapshotAcao> findBySnapshotIdIn(List<Long> snapshotIds);

    long countBySnapshotId(Long snapshotId);

    @Query("SELECT a FROM ProdutoSnapshotAcao a "
            + "WHERE a.snapshot.ano = :ano AND a.snapshot.mes = :mes "
            + "AND (:projetoId IS NULL OR a.snapshot.metaProduto.projetoMeta.projeto.id = :projetoId) "
            + "AND a.statusAcao IN :statusAtivos "
            + "AND a.prazo < :hoje "
            + "ORDER BY a.prazo ASC")
    List<ProdutoSnapshotAcao> findVencidas(
            @Param("ano") Integer ano,
            @Param("mes") Integer mes,
            @Param("projetoId") Long projetoId,
            @Param("statusAtivos") List<StatusAcaoProduto> statusAtivos,
            @Param("hoje") LocalDate hoje
    );
}
