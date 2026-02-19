package com.demandtracker.service;

import com.demandtracker.dto.DemandaAvaliacaoRequestDTO;
import com.demandtracker.dto.DemandaAvaliacaoResponseDTO;
import com.demandtracker.entity.DemandaAvaliacao;
import com.demandtracker.entity.DemandaTecnica;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.TermoEncerramento;
import com.demandtracker.entity.enums.Reutilizacao;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.BusinessException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaAvaliacaoRepository;
import com.demandtracker.repository.DemandaTecnicaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandaAvaliacaoServiceTest {

    @Mock
    private DemandaAvaliacaoRepository avaliacaoRepository;
    @Mock
    private DemandaTecnicaRepository demandaRepository;

    @InjectMocks
    private DemandaAvaliacaoService service;

    @Test
    void findByDemandaIdQuandoNaoExisteLancaResourceNotFoundException() {
        when(avaliacaoRepository.findByDemandaId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByDemandaId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Avaliação não encontrada");
    }

    @Test
    void createQuandoDemandaNaoExisteLancaResourceNotFoundException() {
        when(demandaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(1L, validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Demanda não encontrada");
    }

    @Test
    void createQuandoDemandaNaoEncerradaLancaBusinessException() {
        DemandaTecnica demanda = demanda(1L);
        demanda.setStatus("A");
        when(demandaRepository.findById(1L)).thenReturn(Optional.of(demanda));

        assertThatThrownBy(() -> service.create(1L, validRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Demanda ainda não encerrada");
    }

    @Test
    void createQuandoTermoEncerramentoInexistenteLancaBusinessException() {
        DemandaTecnica demanda = demanda(1L);
        demanda.setStatus(DemandaTecnica.STATUS_ENCERRADA);
        demanda.setTermoEncerramento(null);
        when(demandaRepository.findById(1L)).thenReturn(Optional.of(demanda));

        assertThatThrownBy(() -> service.create(1L, validRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Termo de encerramento inexistente");
    }

    @Test
    void createQuandoJaExisteAvaliacaoLancaBadRequestException() {
        DemandaTecnica demanda = demandaEncerradaComTermo(1L);
        when(demandaRepository.findById(1L)).thenReturn(Optional.of(demanda));
        when(avaliacaoRepository.findByDemandaId(1L)).thenReturn(Optional.of(new DemandaAvaliacao()));

        assertThatThrownBy(() -> service.create(1L, validRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Já existe avaliação");
    }

    @Test
    void createComDadosValidosSalvaERetornaResponse() {
        DemandaTecnica demanda = demandaEncerradaComTermo(1L);
        DemandaAvaliacao saved = new DemandaAvaliacao();
        saved.setId(10L);
        saved.setDemanda(demanda);
        saved.setIndiceSaude(BigDecimal.ONE);
        saved.setIndiceConfiabilidade(BigDecimal.ONE);
        saved.setIndiceRisco(BigDecimal.ZERO);

        when(demandaRepository.findById(1L)).thenReturn(Optional.of(demanda));
        when(avaliacaoRepository.findByDemandaId(1L)).thenReturn(Optional.empty());
        when(avaliacaoRepository.save(any(DemandaAvaliacao.class))).thenReturn(saved);

        DemandaAvaliacaoResponseDTO result = service.create(1L, validRequest());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(avaliacaoRepository).save(any(DemandaAvaliacao.class));
    }

    private static DemandaTecnica demanda(Long id) {
        DemandaTecnica d = new DemandaTecnica();
        d.setId(id);
        d.setProjeto(new Projeto());
        return d;
    }

    private static DemandaTecnica demandaEncerradaComTermo(Long id) {
        DemandaTecnica d = demanda(id);
        d.setStatus(DemandaTecnica.STATUS_ENCERRADA);
        TermoEncerramento termo = new TermoEncerramento();
        termo.setId(100L);
        termo.setDemandaTecnica(d);
        d.setTermoEncerramento(termo);
        return d;
    }

    private static DemandaAvaliacaoRequestDTO validRequest() {
        DemandaAvaliacaoRequestDTO dto = new DemandaAvaliacaoRequestDTO();
        dto.setDataEncerramento(LocalDate.now());
        dto.setAtraso(false);
        dto.setImpactoAtraso(1);
        dto.setImpactoFinanceiro(1);
        dto.setAtendimentoRequisitos(5);
        dto.setEstabilidade(5);
        dto.setRetrabalho(0);
        dto.setSatisfacaoUsuario(5);
        dto.setClarezaRequisitos(5);
        dto.setQualidadePlanejamento(5);
        dto.setAderenciaCronograma(5);
        dto.setComunicacao(5);
        dto.setCapacidadeEquipe(5);
        dto.setDisponibilidadeEquipe(5);
        dto.setPossuiBackupCritico(true);
        dto.setRotatividadeImpactou(false);
        dto.setValorPercebido(5);
        dto.setAlinhamentoMeta(5);
        dto.setReutilizacao(Reutilizacao.ALTA);
        dto.setAvaliacaoGeral(5);
        dto.setRepetiriaModelo(true);
        dto.setRiscos(List.of());
        return dto;
    }
}
