package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "termos_planejamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoPlanejamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demanda_tecnica_id", nullable = false, unique = true)
    private DemandaTecnica demandaTecnica;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String especificacao;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String cronograma;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String resultadoEsperado;
    
    @Column(nullable = false)
    private LocalDateTime dataAbertura;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column
    private LocalDateTime dataAssinatura;
    
    @OneToMany(mappedBy = "termoPlanejamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TermoPlanejamentoCusto> custos;
    
    @PrePersist
    protected void onCreate() {
        dataAbertura = LocalDateTime.now();
    }
}
