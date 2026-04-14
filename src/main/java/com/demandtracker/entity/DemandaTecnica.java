package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "demandas_tecnicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DemandaTecnica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;
    
    @Column(nullable = false, length = 50, unique = true)
    private String codigo;
    
    @Column(nullable = false, length = 200)
    private String nome;
    
    @Column(columnDefinition = "TEXT")
    private String descricao; // Campo opcional para descrição formatada (HTML)
    
    @Column(nullable = false)
    private LocalDateTime dataAbertura;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @OneToOne(mappedBy = "demandaTecnica", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TermoAbertura termoAbertura;
    
    @OneToOne(mappedBy = "demandaTecnica", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TermoPlanejamento termoPlanejamento;
    
    @OneToOne(mappedBy = "demandaTecnica", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TermoEncerramento termoEncerramento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_produto_id", nullable = true)
    private MetaProduto metaProduto;
    
    /**
     * Status = Encerrado e assinado (G).
     * Único status que permite criar avaliação da demanda.
     * @see com.demandtracker.entity.enums.StatusDemandaTecnica
     */
    public static final String STATUS_ENCERRADA = "G";

    @Column(name = "status", nullable = true, length = 1)
    private String status;

    @Column(name = "avaliacao_disponivel")
    private Boolean avaliacaoDisponivel = false;

    @OneToOne(mappedBy = "demanda", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private DemandaAvaliacao avaliacao;

    @OneToOne(mappedBy = "demanda", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private DemandaExecucao execucao;

    @PrePersist
    protected void onCreate() {
        dataAbertura = LocalDateTime.now();
    }
}
