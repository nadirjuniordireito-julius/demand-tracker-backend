package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "termos_encerramento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TermoEncerramento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demanda_tecnica_id", nullable = false, unique = true)
    private DemandaTecnica demandaTecnica;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String resultadoEntregue;
    
    @Column(nullable = false)
    private LocalDateTime dataTermo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column
    private LocalDateTime dataAssinatura;
    
    @Column(name = "data_inicio_execucao", nullable = true)
    private LocalDate dataInicioExecucao;
    
    @Column(name = "data_fim_execucao", nullable = true)
    private LocalDate dataFimExecucao;
    
    @OneToMany(mappedBy = "termoEncerramento", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TermoEncerramentoCusto> custos;

    @OneToMany(mappedBy = "termoEncerramento", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TermoEncerramentoAnexo> anexos;
}
