package com.demandtracker.repository;

import com.demandtracker.entity.TermoEncerramentoAnexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermoEncerramentoAnexoRepository extends JpaRepository<TermoEncerramentoAnexo, Long> {

    List<TermoEncerramentoAnexo> findByTermoEncerramentoIdOrderByIdAsc(Long termoEncerramentoId);

    boolean existsByNomeArquivo(String nomeArquivo);

    void deleteByTermoEncerramentoId(Long termoEncerramentoId);
}
