package com.demandtracker.repository;

import com.demandtracker.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailIgnoreCase(String email);
    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Usuario> findByPerfil(Usuario.UserProfile perfil, Pageable pageable);
    Page<Usuario> findByStatus(Usuario.UserStatus status, Pageable pageable);
    Page<Usuario> findByNomeContainingIgnoreCaseAndPerfil(String nome, Usuario.UserProfile perfil, Pageable pageable);
    Page<Usuario> findByNomeContainingIgnoreCaseAndStatus(String nome, Usuario.UserStatus status, Pageable pageable);
    Page<Usuario> findByPerfilAndStatus(Usuario.UserProfile perfil, Usuario.UserStatus status, Pageable pageable);
    Page<Usuario> findByNomeContainingIgnoreCaseAndPerfilAndStatus(String nome, Usuario.UserProfile perfil, Usuario.UserStatus status, Pageable pageable);
}
