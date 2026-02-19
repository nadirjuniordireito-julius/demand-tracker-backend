package com.demandtracker.repository;

import com.demandtracker.entity.TemplateDemanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateDemandaRepository extends JpaRepository<TemplateDemanda, Long> {
    
    /**
     * Busca templates por ID do Projeto
     */
    List<TemplateDemanda> findByProjetoId(Long projetoId);
    
    /**
     * Busca template por ID do Projeto e Tipo
     */
    Optional<TemplateDemanda> findByProjetoIdAndTipo(Long projetoId, String tipo);
    
    /**
     * Verifica se existe template para um Projeto e Tipo
     */
    boolean existsByProjetoIdAndTipo(Long projetoId, String tipo);
    
    /**
     * Remove templates por ID do Projeto
     */
    void deleteByProjetoId(Long projetoId);
}
