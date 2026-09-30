package com.demandtracker.repository;

import com.demandtracker.entity.Profissional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    Page<Profissional> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Profissional> findByProjetoId(Long projetoId, Pageable pageable);
    Page<Profissional> findByNomeContainingIgnoreCaseAndProjetoId(String nome, Long projetoId, Pageable pageable);

    @Query(
            value = """
                    SELECT p FROM Profissional p
                    LEFT JOIN p.perfil pf
                    WHERE (:profissionalId IS NULL OR p.id = :profissionalId)
                    ORDER BY pf.nome ASC NULLS LAST, p.nome ASC
                    """,
            countQuery = """
                    SELECT COUNT(p) FROM Profissional p
                    WHERE (:profissionalId IS NULL OR p.id = :profissionalId)
                    """
    )
    Page<Profissional> findAllFiltered(@Param("profissionalId") Long profissionalId, Pageable pageable);

    boolean existsByDocumento(String documento);
    boolean existsByDocumentoAndIdNot(String documento, Long id);
}
