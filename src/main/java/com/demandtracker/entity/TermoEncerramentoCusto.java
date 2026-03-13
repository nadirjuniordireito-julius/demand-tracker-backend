package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "termos_encerramento_custos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TermoEncerramentoCusto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "termo_encerramento_id", nullable = false)
    private TermoEncerramento termoEncerramento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal qtdeHora;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;

    @OneToMany(mappedBy = "termoEncerramentoCusto", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TermoEncerramentoCustoProfissional> profissionais = new ArrayList<>();
}
