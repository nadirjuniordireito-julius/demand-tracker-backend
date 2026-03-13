package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nome;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    private UserProfile perfil;
    
    @Column(nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @OneToMany(mappedBy = "usuario", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Projeto> projetos;
    
    @OneToMany(mappedBy = "usuario", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Perfil> perfis;
    
    @OneToMany(mappedBy = "usuario", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<DemandaTecnica> demandas;
    
    @OneToMany(mappedBy = "usuario", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TermoAbertura> termosAbertura;
    
    @OneToMany(mappedBy = "usuario", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TermoPlanejamento> termosPlanejamento;
    
    @OneToMany(mappedBy = "usuario", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TermoEncerramento> termosEncerramento;
    
    @OneToOne(mappedBy = "usuario", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private UsuarioFoto foto;
    
    public enum UserProfile {
        A, O, V
    }
    
    public enum UserStatus {
        A, I
    }
}
