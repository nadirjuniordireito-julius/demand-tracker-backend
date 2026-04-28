package com.demandtracker.repository;

import com.demandtracker.entity.ProfissionalCustoMensal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfissionalCustoMensalRepository extends JpaRepository<ProfissionalCustoMensal, Long> {

    Page<ProfissionalCustoMensal> findByProfissionalId(Long profissionalId, Pageable pageable);

    Page<ProfissionalCustoMensal> findByAno(Integer ano, Pageable pageable);

    Page<ProfissionalCustoMensal> findByMes(Integer mes, Pageable pageable);

    Page<ProfissionalCustoMensal> findByProfissionalIdAndAnoAndMes(Long profissionalId, Integer ano, Integer mes, Pageable pageable);

    List<ProfissionalCustoMensal> findByProfissionalId(Long profissionalId);
}
