package com.demandtracker.repository;

import com.demandtracker.entity.TermoEncerramentoDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermoEncerramentoDocRepository extends JpaRepository<TermoEncerramentoDoc, Long> {
    
    /**
     * Busca documento pelo ID do TermoEncerramento
     */
    Optional<TermoEncerramentoDoc> findByTermoEncerramentoId(Long termoEncerramentoId);
    
    /**
     * Verifica se existe documento para um TermoEncerramento
     */
    boolean existsByTermoEncerramentoId(Long termoEncerramentoId);
    
    /**
     * Remove documento pelo ID do TermoEncerramento
     */
    void deleteByTermoEncerramentoId(Long termoEncerramentoId);

    /**
     * assinar o documento internamente
     */
    // void assinar(Long documentoId, String hashPdf, String ip, String userAgent, Long usuarioId);

        
}