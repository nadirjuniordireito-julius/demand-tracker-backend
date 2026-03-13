package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade para armazenar anexos do Termo de Encerramento (documentos de apoio para consultas).
 * Diferente de TermoEncerramentoDoc, que registra o próprio Termo de Encerramento em PDF.
 * Relacionamento N:1 com TermoEncerramento (vários anexos por termo).
 */
@Entity
@Table(name = "termo_encerramento_anexo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoEncerramentoAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "termo_encerramento_id", nullable = false)
    private TermoEncerramento termoEncerramento;

    /**
     * Arquivo armazenado como byte array.
     * Para PostgreSQL, usar BYTEA diretamente sem @Lob
     */
    @Column(name = "arquivo", columnDefinition = "BYTEA")
    private byte[] arquivo;

    /**
     * Nome do arquivo original
     */
    @Column(name = "nome_arquivo", length = 255, unique = true)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;
}
