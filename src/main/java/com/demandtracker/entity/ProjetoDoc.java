package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade para armazenar documentos de Projeto
 * Relacionamento ManyToOne com Projeto (um projeto pode ter vários documentos)
 */
@Entity
@Table(name = "projeto_doc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoDoc {

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
     * Nome do documento
     * Not null, max 500 caracteres
     */
    @Column(nullable = false, length = 500)
    private String nome;

    /**
     * Documento armazenado como byte array
     * Para PostgreSQL, usar BYTEA diretamente
     */
    @Column(name = "documento", nullable = false, columnDefinition = "BYTEA")
    private byte[] documento;

    /**
     * Nome do arquivo original
     */
    @Column(name = "nome_arquivo", length = 255)
    private String nomeArquivo;

    /**
     * Tipo MIME do arquivo (ex: application/pdf)
     */
    @Column(name = "tipo_conteudo", length = 100)
    private String tipoConteudo;

    /**
     * Tamanho do arquivo em bytes
     */
    @Column(name = "tamanho_arquivo")
    private Long tamanhoArquivo;
}
