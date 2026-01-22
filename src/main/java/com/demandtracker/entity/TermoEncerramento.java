package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "termos_encerramento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoEncerramento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    
    @OneToMany(mappedBy = "termoEncerramento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TermoEncerramentoCusto> custos;
    
    @PrePersist
    protected void onCreate() {
        dataTermo = LocalDateTime.now();
    }
}
