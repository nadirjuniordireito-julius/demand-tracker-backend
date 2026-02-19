package com.demandtracker.repository;

import com.demandtracker.entity.TermoAberturaDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermoAberturaDocRepository extends JpaRepository<TermoAberturaDoc, Long> {
    
    /**
     * Busca documento pelo ID do TermoAbertura
     */
    Optional<TermoAberturaDoc> findByTermoAberturaId(Long termoAberturaId);
    
    /**
     * Verifica se existe documento para um TermoAbertura
     */
    boolean existsByTermoAberturaId(Long termoAberturaId);
    
    /**
     * Remove documento pelo ID do TermoAbertura
     */
    void deleteByTermoAberturaId(Long termoAberturaId);
}
