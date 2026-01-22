package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projetos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Projeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 150)
    private String nome;
    
    @Column(nullable = false, length = 50)
    private String codTed;
    
    @Column(nullable = false)
    private LocalDate termoInicial;
    
    @Column(nullable = false)
    private LocalDate termoFinal;
    
    @Column(nullable = false)
    private LocalDateTime dataUpdate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DemandaTecnica> demandas;
    
    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Perfil> perfis;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        dataUpdate = LocalDateTime.now();
    }
}
