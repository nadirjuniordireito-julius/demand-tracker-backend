package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "termos_planejamento_custos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoPlanejamentoCusto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "termo_planejamento_id", nullable = false)
    private TermoPlanejamento termoPlanejamento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal qtdeHora;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;
}
