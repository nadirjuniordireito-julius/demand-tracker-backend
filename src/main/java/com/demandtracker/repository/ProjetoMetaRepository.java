package com.demandtracker.repository;

import com.demandtracker.entity.ProjetoMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoMetaRepository extends JpaRepository<ProjetoMeta, Long> {
    Page<ProjetoMeta> findByProjetoId(Long projetoId, Pageable pageable);
    Page<ProjetoMeta> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    Page<ProjetoMeta> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<ProjetoMeta> findByStatus(String status, Pageable pageable);
    Page<ProjetoMeta> findByProjetoIdAndCodigoContainingIgnoreCase(Long projetoId, String codigo, Pageable pageable);
    Page<ProjetoMeta> findByProjetoIdAndNomeContainingIgnoreCase(Long projetoId, String nome, Pageable pageable);
    Page<ProjetoMeta> findByProjetoIdAndStatus(Long projetoId, String status, Pageable pageable);
}
