package com.demandtracker.repository;

import com.demandtracker.entity.TermoEncerramentoDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
     * Lista código da demanda e PDF dos termos de encerramento das demandas da meta (ProjetoMeta).
     * Cada elemento: [0] = codigo (String), [1] = arquivoPdf (byte[]).
     */
    @Query("""
        SELECT d.codigo, doc.arquivoPdf
        FROM TermoEncerramentoDoc doc
        JOIN doc.termoEncerramento termo
        JOIN termo.demandaTecnica d
        WHERE d.metaProduto.projetoMeta.id = :projetoMetaId
          AND doc.arquivoPdf IS NOT NULL
        """)
    List<Object[]> findArquivosPdfByProjetoMetaId(@Param("projetoMetaId") Long projetoMetaId);
}