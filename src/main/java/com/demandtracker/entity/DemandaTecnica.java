package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "demandas_tecnicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaTecnica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;
    
    @Column(nullable = false, length = 50, unique = true)
    private String codigo;
    
    @Column(nullable = false, length = 200)
    private String nome;
    
    @Column(nullable = false)
    private LocalDateTime dataAbertura;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @OneToOne(mappedBy = "demandaTecnica", cascade = CascadeType.ALL, orphanRemoval = true)
    private TermoAbertura termoAbertura;
    
    @OneToOne(mappedBy = "demandaTecnica", cascade = CascadeType.ALL, orphanRemoval = true)
    private TermoPlanejamento termoPlanejamento;
    
    @OneToOne(mappedBy = "demandaTecnica", cascade = CascadeType.ALL, orphanRemoval = true)
    private TermoEncerramento termoEncerramento;
    
    @PrePersist
    protected void onCreate() {
        dataAbertura = LocalDateTime.now();
    }
}
