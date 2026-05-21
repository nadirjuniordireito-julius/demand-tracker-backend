package com.demandtracker.repository;

import com.demandtracker.entity.DiaNaoUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DiaNaoUtilRepository extends JpaRepository<DiaNaoUtil, Long> {

    Optional<DiaNaoUtil> findByData(LocalDate data);

    boolean existsByData(LocalDate data);

    boolean existsByDataAndIdNot(LocalDate data, Long id);

    Page<DiaNaoUtil> findByDataGreaterThanEqual(LocalDate dataInicio, Pageable pageable);

    Page<DiaNaoUtil> findByDataLessThanEqual(LocalDate dataFim, Pageable pageable);

    Page<DiaNaoUtil> findByDataBetween(LocalDate dataInicio, LocalDate dataFim, Pageable pageable);

    @Query("SELECT d.data FROM DiaNaoUtil d WHERE d.data >= :inicio AND d.data <= :fim")
    Set<LocalDate> findDatasBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
