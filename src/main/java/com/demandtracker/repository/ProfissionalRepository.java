package com.demandtracker.repository;

import com.demandtracker.entity.Profissional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    Page<Profissional> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Profissional> findByProjetoId(Long projetoId, Pageable pageable);
    Page<Profissional> findByNomeContainingIgnoreCaseAndProjetoId(String nome, Long projetoId, Pageable pageable);

    boolean existsByDocumento(String documento);
    boolean existsByDocumentoAndIdNot(String documento, Long id);
}
