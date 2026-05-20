package com.demandtracker.entity.enums;

import lombok.Getter;

/**
 * Políticas de segurança / status da demanda técnica.
 * Código de um caractere armazenado no banco (demandas_tecnicas.status).
 */
@Getter
public enum StatusDemandaTecnica {
    A("Em elaboração"),
    B("Em abertura"),
    C("Aberta e assinada"),
    D("Em planejamento"),
    E("Em execução"),
    F("Em encerramento"),
    G("Encerrada"),
    Z("Cancelada");

    private final String descricao;

    StatusDemandaTecnica(String descricao) {
        this.descricao = descricao;
    }

    public String getCodigo() {
        return name();
    }

    /** Indica se a demanda está encerrada e assinada (permite avaliação). */
    public boolean isEncerradoAssinado() {
        return this == G;
    }

    /** Permite criar o Termo de Planejamento (demanda em abertura ou já aberta/assinada). */
    public boolean permiteCriacaoTermoPlanejamento() {
        return this == B || this == C;
    }

    /** Permite alterar o Termo de Planejamento (rascunho ou em planejamento). */
    public boolean permiteEdicaoTermoPlanejamento() {
        return this == B || this == C || this == D;
    }

    /** Permite finalizar o planejamento (PDF) ou assinar digitalmente o Termo de Planejamento. */
    public boolean permiteFinalizacaoTermoPlanejamento() {
        return this == C || this == D || this == E;
    }

    /** Permite enviar ou substituir o PDF do Termo de Planejamento. */
    public boolean permiteEnvioDocumentoPlanejamento() {
        return this == B || this == C || this == D || this == E;
    }

    public static StatusDemandaTecnica fromCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return null;
        String c = codigo.trim().toUpperCase();
        if (c.length() != 1) return null;
        try {
            return valueOf(c);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
