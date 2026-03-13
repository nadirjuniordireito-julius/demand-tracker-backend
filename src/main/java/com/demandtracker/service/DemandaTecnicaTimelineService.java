package com.demandtracker.service;

import com.demandtracker.dto.DemandaTecnicaTimelineItemDTO;
import com.demandtracker.dto.UsuarioTimelineDTO;
import com.demandtracker.entity.*;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Monta a timeline (rastro) da demanda técnica a partir das tabelas
 * DemandaTecnica, TermoAbertura, TermoAberturaDoc, TermoPlanejamento, TermoPlanejamentoDoc,
 * TermoEncerramento, TermoEncerramentoDoc.
 */
@Service
@RequiredArgsConstructor
public class DemandaTecnicaTimelineService {

    private final DemandaTecnicaRepository demandaRepository;
    private final TermoAberturaDocRepository termoAberturaDocRepository;
    private final TermoPlanejamentoDocRepository termoPlanejamentoDocRepository;
    private final TermoEncerramentoDocRepository termoEncerramentoDocRepository;
    private final UsuarioFotoRepository usuarioFotoRepository;

    /** C = Criação da demanda técnica */
    public static final String TIPO_CRIACAO = "C";
    /** A = Termo de abertura da demanda */
    public static final String TIPO_ABERTURA = "A";
    /** A1 = Assinatura do termo de abertura */
    public static final String TIPO_ASSINATURA_ABERTURA = "A1";
    /** B = Termo de planejamento */
    public static final String TIPO_PLANEJAMENTO = "B";
    /** B1 = Assinatura do termo de planejamento */
    public static final String TIPO_ASSINATURA_PLANEJAMENTO = "B1";
    /** E = Termo de encerramento */
    public static final String TIPO_ENCERRAMENTO = "E";
    /** E1 = Assinatura do termo de encerramento */
    public static final String TIPO_ASSINATURA_ENCERRAMENTO = "E1";

    @Transactional(readOnly = true)
    public List<DemandaTecnicaTimelineItemDTO> getTimelineByDemandaId(Long demandaTecnicaId) {
        DemandaTecnica demanda = demandaRepository.findById(demandaTecnicaId)
                .orElseThrow(() -> new ResourceNotFoundException("Demanda técnica não encontrada com ID: " + demandaTecnicaId));

        List<Evento> eventos = new ArrayList<>();

        // 1. Criação da demanda técnica
        eventos.add(new Evento(demanda.getDataAbertura(), demanda.getUsuario(), TIPO_CRIACAO));

        TermoAbertura termoAbertura = demanda.getTermoAbertura();
        if (termoAbertura != null) {
            eventos.add(new Evento(termoAbertura.getDataAbertura(), termoAbertura.getUsuario(), TIPO_ABERTURA));
            termoAberturaDocRepository.findByTermoAberturaId(termoAbertura.getId())
                    .filter(doc -> doc.getDataAssinatura() != null)
                    .ifPresent(doc -> eventos.add(new Evento(doc.getDataAssinatura(), doc.getUsuarioSignaturer(), TIPO_ASSINATURA_ABERTURA)));
        }

        TermoPlanejamento termoPlanejamento = demanda.getTermoPlanejamento();
        if (termoPlanejamento != null) {
            eventos.add(new Evento(termoPlanejamento.getDataAbertura(), termoPlanejamento.getUsuario(), TIPO_PLANEJAMENTO));
            termoPlanejamentoDocRepository.findByTermoPlanejamentoId(termoPlanejamento.getId())
                    .filter(doc -> doc.getDataAssinatura() != null)
                    .ifPresent(doc -> eventos.add(new Evento(doc.getDataAssinatura(), doc.getUsuarioSignaturer(), TIPO_ASSINATURA_PLANEJAMENTO)));
        }

        TermoEncerramento termoEncerramento = demanda.getTermoEncerramento();
        if (termoEncerramento != null) {
            eventos.add(new Evento(termoEncerramento.getDataTermo(), termoEncerramento.getUsuario(), TIPO_ENCERRAMENTO));
            termoEncerramentoDocRepository.findByTermoEncerramentoId(termoEncerramento.getId())
                    .filter(doc -> doc.getDataAssinatura() != null)
                    .ifPresent(doc -> eventos.add(new Evento(doc.getDataAssinatura(), doc.getUsuarioSignaturer(), TIPO_ASSINATURA_ENCERRAMENTO)));
        }

        // Ordem fixa: C → A → A1 → B → B1 → E → E1 (já montada acima; não ordenar por data)
        List<DemandaTecnicaTimelineItemDTO> resultado = new ArrayList<>();
        int sequencia = 1;
        for (Evento ev : eventos) {
            resultado.add(new DemandaTecnicaTimelineItemDTO(
                    sequencia++,
                    ev.getDataHora(),
                    toUsuarioTimelineDTO(ev.getUsuario()),
                    ev.getTipo()
            ));
        }
        return resultado;
    }

    private UsuarioTimelineDTO toUsuarioTimelineDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        String fotoUrl = usuarioFotoRepository.existsByUsuarioId(usuario.getId())
                ? "/api/usuario-foto/usuario/" + usuario.getId() + "/download"
                : null;
        return new UsuarioTimelineDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), fotoUrl);
    }

    private static class Evento {
        private final LocalDateTime dataHora;
        private final Usuario usuario;
        private final String tipo;

        Evento(LocalDateTime dataHora, Usuario usuario, String tipo) {
            this.dataHora = dataHora;
            this.usuario = usuario;
            this.tipo = tipo;
        }

        LocalDateTime getDataHora() { return dataHora; }
        Usuario getUsuario() { return usuario; }
        String getTipo() { return tipo; }
    }
}
