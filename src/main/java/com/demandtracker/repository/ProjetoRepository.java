package com.demandtracker.repository;

import com.demandtracker.entity.Projeto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
    Page<Projeto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Projeto> findByCodTedContainingIgnoreCase(String codTed, Pageable pageable);
    Page<Projeto> findByUsuarioId(Long usuarioId, Pageable pageable);
    Page<Projeto> findByNomeContainingIgnoreCaseAndCodTedContainingIgnoreCase(String nome, String codTed, Pageable pageable);
}
