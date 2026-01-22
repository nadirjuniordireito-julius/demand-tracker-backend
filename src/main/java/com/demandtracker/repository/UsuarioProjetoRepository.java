package com.demandtracker.repository;

import com.demandtracker.entity.UsuarioProjeto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioProjetoRepository extends JpaRepository<UsuarioProjeto, Long> {
    Page<UsuarioProjeto> findByUsuarioId(Long usuarioId, Pageable pageable);
    Page<UsuarioProjeto> findByProjetoId(Long projetoId, Pageable pageable);
    Page<UsuarioProjeto> findByUsuarioIdAndProjetoId(Long usuarioId, Long projetoId, Pageable pageable);
    Optional<UsuarioProjeto> findByUsuarioIdAndProjetoId(Long usuarioId, Long projetoId);
    boolean existsByUsuarioIdAndProjetoId(Long usuarioId, Long projetoId);
}
