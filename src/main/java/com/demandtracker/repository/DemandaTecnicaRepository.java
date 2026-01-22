package com.demandtracker.repository;

import com.demandtracker.entity.DemandaTecnica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DemandaTecnicaRepository extends JpaRepository<DemandaTecnica, Long> {
    Optional<DemandaTecnica> findByCodigo(String codigo);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    Page<DemandaTecnica> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<DemandaTecnica> findByProjetoId(Long projetoId, Pageable pageable);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCase(String codigo, String nome, Pageable pageable);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCaseAndProjetoId(String codigo, Long projetoId, Pageable pageable);
    Page<DemandaTecnica> findByNomeContainingIgnoreCaseAndProjetoId(String nome, Long projetoId, Pageable pageable);
    Page<DemandaTecnica> findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCaseAndProjetoId(String codigo, String nome, Long projetoId, Pageable pageable);
    
    @Query("SELECT COUNT(d) FROM DemandaTecnica d")
    Long countTotal();
    
    @Query("SELECT COUNT(d) FROM DemandaTecnica d WHERE d.termoAbertura IS NULL")
    Long countAbertas();
    
    @Query("SELECT COUNT(d) FROM DemandaTecnica d WHERE d.termoEncerramento IS NOT NULL AND d.termoEncerramento.dataAssinatura IS NOT NULL")
    Long countEncerradas();
}
