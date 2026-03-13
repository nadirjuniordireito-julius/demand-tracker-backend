package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidade para armazenar os profissionais (cpf,cnpj) da operação do Projeto
 */
@Entity
@Table(name = "profissional")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relacionamento ManyToOne com Projeto
     * Not null
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    /**
     * tipo de pessoa (F-Fisica, J-Juridica)
     * Not null, max 1 caracteres
     */
    @Column(nullable = false, length = 1)
    private String tipoPessoa;

    /**
     * cpf para pessoa fisica ou cnpj para pessoa juridica
     */
    @Column(name = "documento", nullable = false, unique = true)
    private String documento;

    /**
     * Nome do documento
     * Not null, max 500 caracteres
     */
    @Column(nullable = false, length = 500)
    private String nome;


    /**
     * valor hora do profissional
     * Not null, precision 10, scale 2
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;

    /**
     * data de inicio da atividade do profissional
     * Not null
     */
    @Column(nullable = false)
    private LocalDate dataInicioAtividade;

    /**
     * Função/cargo do profissional (opcional).
     */
    @Column(name = "funcao", length = 255, nullable = true)
    private String funcao;

        /**
     * Relacionamento ManyToOne com Perfil
     * Not null
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = true)
    private Perfil perfil;
    
}
