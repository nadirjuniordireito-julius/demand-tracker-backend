package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "projeto_meta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;
    
    @Column(nullable = false, length = 10)
    private String codigo;
    
    @Column(nullable = false, length = 500)
    private String nome;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @Column(nullable = false, length = 1)
    private String status;
    
    @OneToMany(mappedBy = "projetoMeta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetaProduto> produtos;
}
