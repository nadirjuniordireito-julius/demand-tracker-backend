package com.demandtracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "meta_produto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_meta_id", nullable = false)
    private ProjetoMeta projetoMeta;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(nullable = false, length = 500)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 100)
    private String unidadeMedida;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal valorUnitario;

    @Column(nullable = false)
    private Integer inicio;

    @Column(nullable = false)
    private Integer fim;

    /** Data de início do produto (campo opcional, apenas data, sem hora). */
    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    /** Data de fim do produto (campo opcional, apenas data, sem hora). */
    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(nullable = false, length = 1)
    private String status;

    @Column(nullable = true)
    private Integer percExecutado;

    /** A cada n meses, a entrega é feita */
    @Transient
    private Integer intervaloEntregaEmMeses;

    public Integer getIntervaloEntregaEmMeses() {
        if (this.inicio == null || this.fim == null) {
            return 0;
        }
        Integer resultado = this.quantidade;
        if( this.quantidade > 1) {
            resultado = (this.fim - this.inicio + 1) / this.quantidade;
        }
        return resultado;
    }

    @Transient
    private LocalDate dataPrimeiraEntrega;

    public LocalDate getDataPrimeiraEntrega() {
        if (this.dataInicio == null) {
            return null;
        }
        return this.dataInicio.plusMonths(getIntervaloEntregaEmMeses());
    }

}
