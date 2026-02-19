package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade para armazenar documentos PDF dos Termos de Planejamento
 * Relacionamento 1:1 com TermoPlanejamento
 */
@Entity
@Table(name = "termo_planejamento_doc", 
       uniqueConstraints = @UniqueConstraint(columnNames = "termo_planejamento_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoPlanejamentoDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relacionamento 1:1 com TermoPlanejamento
     * Unique constraint garante que cada termo tenha apenas um documento
     */
    @OneToOne
    @JoinColumn(name = "termo_planejamento_id", nullable = false, unique = true)
    private TermoPlanejamento termoPlanejamento;


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
