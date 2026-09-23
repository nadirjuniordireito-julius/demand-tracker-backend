package com.demandtracker.repository;

import com.demandtracker.entity.TermoAberturaDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermoAberturaDocRepository extends JpaRepository<TermoAberturaDoc, Long> {

    /** Busca documento pelo ID do TermoAbertura. */
    Optional<TermoAberturaDoc> findByTermoAberturaId(Long termoAberturaId);

    /** Verifica se existe documento para um TermoAbertura. */
    boolean existsByTermoAberturaId(Long termoAberturaId);

    /** Remove documento pelo ID do TermoAbertura. */
    void deleteByTermoAberturaId(Long termoAberturaId);

    /**
     * Lista código da demanda e PDF dos termos de abertura das demandas da meta (ProjetoMeta).
     * Cada elemento: [0] = codigo (String), [1] = arquivoPdf (byte[]).
     */
    @Query("""
        SELECT d.codigo, doc.arquivoPdf
        FROM TermoAberturaDoc doc
        JOIN doc.termoAbertura termo
        JOIN termo.demandaTecnica d
        WHERE d.metaProduto.projetoMeta.id = :projetoMetaId
          AND doc.arquivoPdf IS NOT NULL
        """)
    List<Object[]> findArquivosPdfByProjetoMetaId(@Param("projetoMetaId") Long projetoMetaId);
}
