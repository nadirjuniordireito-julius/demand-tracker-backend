package com.demandtracker.repository;

import com.demandtracker.entity.TermoAbertura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermoAberturaRepository extends JpaRepository<TermoAbertura, Long> {
    Optional<TermoAbertura> findByDemandaTecnicaId(Long demandaTecnicaId);
}
