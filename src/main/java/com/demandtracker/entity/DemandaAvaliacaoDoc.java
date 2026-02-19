package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade para armazenar documento PDF da Avaliação da Demanda.
 * Relacionamento 1:1 com DemandaAvaliacao.
 */
@Entity
@Table(name = "demanda_avaliacao_doc",
       uniqueConstraints = @UniqueConstraint(columnNames = "avaliacao_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaAvaliacaoDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "avaliacao_id", nullable = false, unique = true)
    private DemandaAvaliacao avaliacao;

    @Column(name = "data_upload")
    private LocalDateTime dataUpload;

    @Column(name = "arquivo_pdf", columnDefinition = "BYTEA")
    private byte[] arquivoPdf;

    @Column(name = "nome_arquivo", length = 255)
    private String nomeArquivo;

    @Column(name = "tipo_conteudo", length = 100)
    private String tipoConteudo;

    @Column(name = "tamanho_arquivo")
    private Long tamanhoArquivo;
}
