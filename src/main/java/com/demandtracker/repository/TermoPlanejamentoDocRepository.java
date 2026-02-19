package com.demandtracker.repository;

import com.demandtracker.entity.TermoPlanejamentoDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermoPlanejamentoDocRepository extends JpaRepository<TermoPlanejamentoDoc, Long> {
    
    /**
     * Busca documento pelo ID do TermoPlanejamento
     */
    Optional<TermoPlanejamentoDoc> findByTermoPlanejamentoId(Long termoPlanejamentoId);
    
    /**
     * Verifica se existe documento para um TermoPlanejamento
     */
    boolean existsByTermoPlanejamentoId(Long termoPlanejamentoId);
    
    /**
     * Remove documento pelo ID do TermoPlanejamento
     */
    void deleteByTermoPlanejamentoId(Long termoPlanejamentoId);
}
