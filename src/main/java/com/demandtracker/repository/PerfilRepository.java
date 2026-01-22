package com.demandtracker.repository;

import com.demandtracker.entity.Perfil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Page<Perfil> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
