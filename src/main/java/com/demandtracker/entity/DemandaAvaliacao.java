package com.demandtracker.entity;

import com.demandtracker.entity.enums.Reutilizacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demanda_avaliacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DemandaAvaliacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demanda_tecnica_id", nullable = false, unique = true)
    private DemandaTecnica demanda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "data_hora_preenchimento")
    private LocalDateTime dataHoraPreenchimento;

    @Column(name = "termo_encerramento_id")
    private Long termoEncerramentoId;

    @Column(name = "data_avaliacao")
    private LocalDateTime dataAvaliacao;

    @Column(name = "data_encerramento")
    private LocalDate dataEncerramento;

    @Column(name = "atraso")
    private Boolean atraso;

    @Column(name = "desvio_prazo_percentual", precision = 10, scale = 2)
    private BigDecimal desvioPrazoPercentual;

    @Column(name = "impacto_atraso")
    private Integer impactoAtraso;

    @Column(name = "desvio_custo_percentual", precision = 10, scale = 2)
    private BigDecimal desvioCustoPercentual;

    @Column(name = "impacto_financeiro")
    private Integer impactoFinanceiro;

    @Column(name = "atendimento_requisitos")
    private Integer atendimentoRequisitos;

    @Column(name = "estabilidade")
    private Integer estabilidade;

    @Column(name = "retrabalho")
    private Integer retrabalho;

    @Column(name = "satisfacao_usuario")
    private Integer satisfacaoUsuario;

    @Column(name = "clareza_requisitos")
    private Integer clarezaRequisitos;

    @Column(name = "qualidade_planejamento")
    private Integer qualidadePlanejamento;

    @Column(name = "aderencia_cronograma")
    private Integer aderenciaCronograma;

    @Column(name = "comunicacao")
    private Integer comunicacao;

    @Column(name = "capacidade_equipe")
    private Integer capacidadeEquipe;

    @Column(name = "disponibilidade_equipe")
    private Integer disponibilidadeEquipe;

    @Column(name = "possui_backup_critico")
    private Boolean possuiBackupCritico;

    @Column(name = "rotatividade_impactou")
    private Boolean rotatividadeImpactou;

    @Column(name = "valor_percebido")
    private Integer valorPercebido;

    @Column(name = "alinhamento_meta")
    private Integer alinhamentoMeta;

    @Enumerated(EnumType.STRING)
    @Column(name = "reutilizacao", length = 10)
    private Reutilizacao reutilizacao;

    @Column(name = "avaliacao_geral")
    private Integer avaliacaoGeral;

    @Column(name = "repetiria_modelo")
    private Boolean repetiriaModelo;

    @Column(name = "indice_saude", precision = 10, scale = 4)
    private BigDecimal indiceSaude;

    @Column(name = "indice_confiabilidade", precision = 10, scale = 4)
    private BigDecimal indiceConfiabilidade;

    @Column(name = "indice_risco", precision = 10, scale = 4)
    private BigDecimal indiceRisco;

    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DemandaAvaliacaoRisco> riscos = new ArrayList<>();

    @OneToOne(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private DemandaAvaliacaoTexto texto;

    @OneToOne(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private DemandaAvaliacaoDoc doc;

    @PrePersist
    @PreUpdate
    protected void onPersistOrUpdate() {
        dataHoraPreenchimento = LocalDateTime.now();
    }
}
