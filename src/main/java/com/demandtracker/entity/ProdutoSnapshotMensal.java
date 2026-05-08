package com.demandtracker.entity;

import com.demandtracker.entity.enums.StatusProdutoMes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Snapshot mensal por produto (fotografia mensal congelada no fechamento do mês).
 * Único por (metaProduto, ano, mes).
 */
@Entity
@Table(name = "produto_snapshot_mensal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProdutoSnapshotMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_produto_id", nullable = false)
    private MetaProduto metaProduto;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private Integer mes;

    /**
     * Status do produto no mês: V=Verde, A=Amarelo, R=Vermelho.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_produto_mes", nullable = false, length = 1)
    private StatusProdutoMes statusProdutoMes;

    @Column(length = 255)
    private String situacao;

    @Column(name = "valor_total_orcamento", precision = 18, scale = 2)
    private BigDecimal valorTotalOrcamento;

    @Column(name = "valor_total_em_execucao", precision = 18, scale = 2)
    private BigDecimal valorTotalEmExecucao;

    @Column(name = "valor_total_executado", precision = 18, scale = 2)
    private BigDecimal valorTotalExecutado;

    @Column(name = "percentual_execucao", precision = 10, scale = 2)
    private BigDecimal percentualExecucao;

    @Column(name = "valor_media_entrega_prevista_mensal", precision = 18, scale = 2)
    private BigDecimal valorMediaEntregaPrevistaMensal;

    @Column(name = "valor_media_entrega_real_mensal", precision = 18, scale = 2)
    private BigDecimal valorMediaEntregaRealMensal;

    /**
     * Diagnóstico do mês registrado pelo gestor/operador.
     */
    @Column(name = "resumo_analitico", columnDefinition = "TEXT")
    private String resumoAnalitico;

    /**
     * Quando true, snapshot está travado (não permite alterar campos do diagnóstico).
     * Ações vinculadas continuam podendo evoluir mesmo após o fechamento.
     */
    @Column(nullable = false)
    private Boolean fechado = Boolean.FALSE;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_fechamento_id")
    private Usuario usuarioFechamento;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @Column(name = "data_update", nullable = false)
    private LocalDateTime dataUpdate;

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        if (this.dataRegistro == null) {
            this.dataRegistro = agora;
        }
        this.dataUpdate = agora;
        if (this.fechado == null) {
            this.fechado = Boolean.FALSE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dataUpdate = LocalDateTime.now();
    }
}
