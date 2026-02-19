package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade para armazenar foto do usuário
 * Relacionamento 1:1 com Usuario (unique constraint garante apenas uma foto por usuário)
 */
@Entity
@Table(name = "usuario_foto", 
       uniqueConstraints = @UniqueConstraint(columnNames = "usuario_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioFoto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Relacionamento 1:1 com Usuario
     * Unique constraint garante que cada usuário tenha apenas uma foto
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
    
    /**
     * Foto armazenada como byte array
     * Para PostgreSQL, usar BYTEA diretamente
     */
    @Column(name = "foto", nullable = false, columnDefinition = "BYTEA")
    private byte[] foto;
}
