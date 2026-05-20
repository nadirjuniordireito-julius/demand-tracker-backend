package com.demandtracker.repository;

import com.demandtracker.entity.ProfissionalCustoMensal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfissionalCustoMensalRepository extends JpaRepository<ProfissionalCustoMensal, Long> {

    @Query(
            value = """
                    SELECT pcm FROM ProfissionalCustoMensal pcm
                    JOIN pcm.profissional p
                    LEFT JOIN p.perfil pf
                    WHERE (:profissionalId IS NULL OR p.id = :profissionalId)
                      AND (:ano IS NULL OR pcm.ano = :ano)
                      AND (:mes IS NULL OR pcm.mes = :mes)
                    ORDER BY pf.nome ASC NULLS LAST, p.nome ASC
                    """,
            countQuery = """
                    SELECT COUNT(pcm) FROM ProfissionalCustoMensal pcm
                    JOIN pcm.profissional p
                    WHERE (:profissionalId IS NULL OR p.id = :profissionalId)
                      AND (:ano IS NULL OR pcm.ano = :ano)
                      AND (:mes IS NULL OR pcm.mes = :mes)
                    """
    )
    Page<ProfissionalCustoMensal> findAllFiltered(
            @Param("profissionalId") Long profissionalId,
            @Param("ano") Integer ano,
            @Param("mes") Integer mes,
            Pageable pageable
    );

    Page<ProfissionalCustoMensal> findByProfissionalId(Long profissionalId, Pageable pageable);

    Page<ProfissionalCustoMensal> findByAno(Integer ano, Pageable pageable);

    Page<ProfissionalCustoMensal> findByMes(Integer mes, Pageable pageable);

    Page<ProfissionalCustoMensal> findByProfissionalIdAndAnoAndMes(Long profissionalId, Integer ano, Integer mes, Pageable pageable);

    List<ProfissionalCustoMensal> findByProfissionalId(Long profissionalId);
}
