package com.demandtracker.repository;

import com.demandtracker.entity.DemandaAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandaAvaliacaoRepository extends JpaRepository<DemandaAvaliacao, Long> {
    Optional<DemandaAvaliacao> findByDemandaId(Long demandaId);

    /** Carrega avaliação com texto (riscos são lazy e carregados ao acessar). Evita 2 JOIN FETCH na mesma query. */
    @Query("SELECT DISTINCT a FROM DemandaAvaliacao a " +
           "LEFT JOIN FETCH a.texto " +
           "WHERE a.demanda.id = :demandaId")
    Optional<DemandaAvaliacao> findByDemandaIdWithRiscosETexto(Long demandaId);

    @Query("SELECT DISTINCT a FROM DemandaAvaliacao a " +
           "LEFT JOIN FETCH a.demanda d " +
           "LEFT JOIN FETCH d.projeto " +
           "LEFT JOIN FETCH d.metaProduto mp " +
           "LEFT JOIN FETCH mp.projetoMeta")
    List<DemandaAvaliacao> findAllWithDemandaAndProjetoAndMeta();
}
