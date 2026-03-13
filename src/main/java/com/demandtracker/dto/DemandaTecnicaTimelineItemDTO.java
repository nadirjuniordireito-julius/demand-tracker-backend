package com.demandtracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Um registro da timeline do rastro da demanda técnica.
 * Ordenado por data/hora do evento; sequencia indica a ordem (1, 2, 3...).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaTecnicaTimelineItemDTO {

    /** Ordem do evento na timeline (1-based). */
    private Integer sequencia;

    /** Data e hora do evento. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraEvento;

    /** Usuário associado ao evento (pode ser nulo se não houver, ex.: assinatura sem usuário). */
    private UsuarioTimelineDTO usuario;

    /**
     * Tipo do evento:
     * C = Criação da demanda técnica
     * A = Termo de abertura da demanda
     * A1 = Assinatura do termo de abertura
     * B = Termo de planejamento
     * B1 = Assinatura do termo de planejamento
     * E = Termo de encerramento
     * E1 = Assinatura do termo de encerramento
     */
    private String tipoEvento;
}
