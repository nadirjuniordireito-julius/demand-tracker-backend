package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade para armazenar documentos PDF dos Termos de Encerramento
 * Relacionamento 1:1 com TermoEncerramento
 */
@Entity
@Table(name = "termo_encerramento_doc", 
       uniqueConstraints = @UniqueConstraint(columnNames = "termo_encerramento_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoEncerramentoDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relacionamento 1:1 com TermoEncerramento
     * Unique constraint garante que cada termo tenha apenas um documento
     */
    @OneToOne
    @JoinColumn(name = "termo_encerramento_id", nullable = false, unique = true)
    private TermoEncerramento termoEncerramento;

    /**
     * Arquivo PDF armazenado como byte array
     * Para PostgreSQL, usar BYTEA diretamente sem @Lob
     */
    @Column(name = "arquivo_pdf", columnDefinition = "BYTEA")
    private byte[] arquivoPdf;

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


    /**
     * dados para controle da assinatura eletronica
     */

    /**
     * Data de assinatura do documento
     */
    @Column(name = "data_assinatura", nullable = true)
    private LocalDateTime dataAssinatura;

    @Column(name = "hashPdf", nullable = true)
    private String hashPdf;

    @Column(name = "ip", nullable = true)
    private String ip;

    @Column(name = "userAgent", nullable = true)
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarioSignaturer", nullable = true)
    private Usuario usuarioSignaturer;
}
