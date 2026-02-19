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
    E("Planejado e assinado"),
    F("Em encerramento"),
    G("Encerrado e assinado"),
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
