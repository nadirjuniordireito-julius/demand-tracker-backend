package com.demandtracker.repository;

import com.demandtracker.entity.TermoPlanejamentoDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Lista código da demanda e PDF dos termos de planejamento das demandas da meta (ProjetoMeta).
     * Cada elemento: [0] = codigo (String), [1] = arquivoPdf (byte[]).
     */
    @Query("""
        SELECT d.codigo, doc.arquivoPdf
        FROM TermoPlanejamentoDoc doc
        JOIN doc.termoPlanejamento termo
        JOIN termo.demandaTecnica d
        WHERE d.metaProduto.projetoMeta.id = :projetoMetaId
          AND doc.arquivoPdf IS NOT NULL
        """)
    List<Object[]> findArquivosPdfByProjetoMetaId(@Param("projetoMetaId") Long projetoMetaId);
}
