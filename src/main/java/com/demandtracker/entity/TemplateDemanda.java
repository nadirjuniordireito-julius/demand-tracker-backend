package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade para armazenar templates de demanda em formato DOCX
 * Relacionamento ManyToOne com Projeto
 */
@Entity
@Table(name = "template_demanda")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDemanda {

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
     * Tipo do template (char(1))
     * Not null
     */
    @Column(name = "tipo", nullable = false, length = 1)
    private String tipo;

    /**
     * Arquivo DOCX armazenado como byte array
     * Para PostgreSQL, usar BYTEA diretamente
     */
    @Column(name = "arquivo_docx", columnDefinition = "BYTEA")
    private byte[] arquivoDocx;

    /**
     * Nome do arquivo original
     */
    @Column(name = "nome_arquivo", length = 255)
    private String nomeArquivo;

    /**
     * Tipo MIME do arquivo (ex: application/vnd.openxmlformats-officedocument.wordprocessingml.document)
     */
    @Column(name = "tipo_conteudo", length = 100)
    private String tipoConteudo;

    /**
     * Tamanho do arquivo em bytes
     */

    @Column(name = "tamanho_arquivo")
    private Long tamanhoArquivo;
}
