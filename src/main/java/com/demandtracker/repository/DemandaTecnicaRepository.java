package com.demandtracker.repository;

import com.demandtracker.entity.DemandaTecnica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandaTecnicaRepository extends JpaRepository<DemandaTecnica, Long> {

    @Query("SELECT d FROM DemandaTecnica d " +
           "LEFT JOIN FETCH d.metaProduto mp " +
           "LEFT JOIN FETCH mp.projetoMeta " +
           "WHERE d.id = :id")
    Optional<DemandaTecnica> findByIdWithMeta(@Param("id") Long id);

    Optional<DemandaTecnica> findByCodigo(String codigo);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    Page<DemandaTecnica> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<DemandaTecnica> findByProjetoId(Long projetoId, Pageable pageable);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCase(String codigo, String nome, Pageable pageable);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCaseAndProjetoId(String codigo, Long projetoId, Pageable pageable);
    Page<DemandaTecnica> findByNomeContainingIgnoreCaseAndProjetoId(String nome, Long projetoId, Pageable pageable);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCaseAndProjetoId(String codigo, String nome, Long projetoId, Pageable pageable);
    Page<DemandaTecnica> findByStatus(String status, Pageable pageable);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCaseAndStatus(String codigo, String status, Pageable pageable);
    Page<DemandaTecnica> findByNomeContainingIgnoreCaseAndStatus(String nome, String status, Pageable pageable);
    Page<DemandaTecnica> findByProjetoIdAndStatus(Long projetoId, String status, Pageable pageable);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCaseAndStatus(String codigo, String nome, String status, Pageable pageable);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCaseAndProjetoIdAndStatus(String codigo, Long projetoId, String status, Pageable pageable);
    Page<DemandaTecnica> findByNomeContainingIgnoreCaseAndProjetoIdAndStatus(String nome, Long projetoId, String status, Pageable pageable);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCaseAndProjetoIdAndStatus(String codigo, String nome, Long projetoId, String status, Pageable pageable);

    List<DemandaTecnica> findByMetaProdutoId(Long metaProdutoId);

    /** Demandas do produto excluindo as canceladas (status Z). Para semáforo. */
    List<DemandaTecnica> findByMetaProdutoIdAndStatusNot(Long metaProdutoId, String status);

    long countByMetaProdutoId(Long metaProdutoId);

    /** Conta demandas do produto excluindo as canceladas (status Z). Para semáforo. */
    long countByMetaProdutoIdAndStatusNot(Long metaProdutoId, String status);
    /** Quantidade de demandas cujo produto pertence à meta (projetoMeta). Usado para sequência do código automático. */
    long countByMetaProdutoProjetoMetaId(Long projetoMetaId);

    /** Última demanda (maior código) para a meta (projetoMeta) informada. Usado para descobrir a próxima sequência do código automático. */
    Optional<DemandaTecnica> findFirstByMetaProdutoProjetoMetaIdOrderByCodigoDesc(Long projetoMetaId);

    long countByMetaProdutoIdAndStatus(Long metaProdutoId, String status);

    @Query("SELECT COUNT(d) FROM DemandaTecnica d")
    Long countTotal();
    
    @Query("SELECT COUNT(d) FROM DemandaTecnica d WHERE d.termoAbertura IS NULL")
    Long countAbertas();
    
    @Query("SELECT COUNT(d) FROM DemandaTecnica d WHERE d.termoEncerramento IS NOT NULL AND d.termoEncerramento.dataAssinatura IS NOT NULL")
    Long countEncerradas();

    @Query("""
    SELECT d FROM DemandaTecnica d
    WHERE d.projeto.id = :projetoId
      AND d.status IN ('B','D','F')
    """)
    Page<DemandaTecnica> findByProjetoIdAndStatusEmFluxo(
        @Param("projetoId") Long projetoId,
        Pageable pageable
    );
    
}
