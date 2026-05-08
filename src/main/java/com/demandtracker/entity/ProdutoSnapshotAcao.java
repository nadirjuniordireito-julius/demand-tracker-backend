package com.demandtracker.entity;

import com.demandtracker.entity.enums.ImpactoAcao;
import com.demandtracker.entity.enums.StatusAcaoProduto;
import com.demandtracker.entity.enums.TipoAcaoProduto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Plano de ação vinculado a um snapshot mensal do produto.
 */
@Entity
@Table(name = "produto_snapshot_acao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProdutoSnapshotAcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private ProdutoSnapshotMensal snapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acao", nullable = false, length = 20)
    private TipoAcaoProduto tipoAcao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    /**
     * Usuário responsável (preferencial). Pode ficar nulo se a ação for atribuída
     * a alguém externo, registrado apenas em {@code responsavelNome}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    /**
     * Nome do responsável quando não há usuário cadastrado (registro histórico).
     */
    @Column(name = "responsavel_nome", length = 255)
    private String responsavelNome;

    @Column(nullable = false)
    private LocalDate prazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private ImpactoAcao impacto;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_acao", nullable = false, length = 20)
    private StatusAcaoProduto statusAcao;

    @Column(name = "data_status", nullable = false)
    private LocalDateTime dataStatus;

    @Column(name = "observacao_status", columnDefinition = "TEXT")
    private String observacaoStatus;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_update", nullable = false)
    private LocalDateTime dataUpdate;

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        if (this.dataCriacao == null) {
            this.dataCriacao = agora;
        }
        if (this.dataStatus == null) {
            this.dataStatus = agora;
        }
        this.dataUpdate = agora;
        if (this.statusAcao == null) {
            this.statusAcao = StatusAcaoProduto.ABERTA;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dataUpdate = LocalDateTime.now();
    }
}
