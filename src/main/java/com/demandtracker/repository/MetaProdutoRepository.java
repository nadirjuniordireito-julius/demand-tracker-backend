package com.demandtracker.repository;

import com.demandtracker.entity.MetaProduto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MetaProdutoRepository extends JpaRepository<MetaProduto, Long> {

    @Query("SELECT COALESCE(SUM(p.quantidade * p.valorUnitario), 0) FROM MetaProduto p WHERE p.projetoMeta.projeto.id = :projetoId")
    BigDecimal sumValorTotalByProjetoId(@Param("projetoId") Long projetoId);

    /** Retorna todos os produtos de todas as metas de um projeto. */
    List<MetaProduto> findByProjetoMetaProjetoId(Long projetoId);

    Page<MetaProduto> findByProjetoMetaId(Long projetoMetaId, Pageable pageable);
    Page<MetaProduto> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    Page<MetaProduto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<MetaProduto> findByStatus(String status, Pageable pageable);
    Page<MetaProduto> findByProjetoMetaIdAndCodigoContainingIgnoreCase(Long projetoMetaId, String codigo, Pageable pageable);
    Page<MetaProduto> findByProjetoMetaIdAndNomeContainingIgnoreCase(Long projetoMetaId, String nome, Pageable pageable);
    Page<MetaProduto> findByProjetoMetaIdAndStatus(Long projetoMetaId, String status, Pageable pageable);
}
