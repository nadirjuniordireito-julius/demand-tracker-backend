package com.demandtracker.repository;

import com.demandtracker.entity.UsuarioFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioFotoRepository extends JpaRepository<UsuarioFoto, Long> {
    Optional<UsuarioFoto> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
}
