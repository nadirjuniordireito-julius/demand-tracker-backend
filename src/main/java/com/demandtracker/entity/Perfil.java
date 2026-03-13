package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "perfis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 150)
    private String nome;
    
    @Column(nullable = false)
    private LocalDate termoInicial;
    
    @Column(nullable = false)
    private LocalDate termoFinal;
    
    @Column(nullable = false)
    private LocalDateTime dataUpdate;
    
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;
    
    @OneToMany(mappedBy = "perfil", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TermoPlanejamentoCusto> custosPlanejamento;
    
    @OneToMany(mappedBy = "perfil", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TermoEncerramentoCusto> custosEncerramento;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        dataUpdate = LocalDateTime.now();
    }
}
