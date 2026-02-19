package com.demandtracker.repository;

import com.demandtracker.entity.ProjetoDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetoDocRepository extends JpaRepository<ProjetoDoc, Long> {
    
    /**
     * Busca documentos pelo ID do Projeto
     */
    List<ProjetoDoc> findByProjetoId(Long projetoId);
    
    /**
     * Verifica se existe documento para um Projeto
     */
    boolean existsByProjetoId(Long projetoId);
    
    /**
     * Remove documentos pelo ID do Projeto
     */
    void deleteByProjetoId(Long projetoId);
}
