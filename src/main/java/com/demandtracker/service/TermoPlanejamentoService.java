package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.*;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demandtracker.entity.enums.StatusDemandaTecnica;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.demandtracker.entity.DemandaTecnica;

@Service
@RequiredArgsConstructor
public class TermoPlanejamentoService {
    
    private final TermoPlanejamentoRepository termoRepository;
    private final TermoPlanejamentoCustoRepository termoPlanejamentoCustoRepository;
    private final DemandaTecnicaRepository demandaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;
    private final TermoAberturaRepository termoAberturaRepository;
    
    public Page<TermoPlanejamentoDTO> findAll(Pageable pageable) {
        return termoRepository.findAll(pageable).map(TermoPlanejamentoDTO::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public TermoPlanejamentoDTO findById(Long id) {
        TermoPlanejamento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + id));
        TermoPlanejamentoDTO dto = TermoPlanejamentoDTO.fromEntity(termo);
        preencherValoresProdutoNoModal(dto, termo.getDemandaTecnica());
        return dto;
    }
    
    @Transactional(readOnly = true)
    public TermoPlanejamentoDTO findByDemandaId(Long demandaId) {
        TermoPlanejamento termo = termoRepository.findByDemandaTecnicaId(demandaId).orElse(null);
        if (termo == null) {
            return null;
        }
        TermoPlanejamentoDTO dto = TermoPlanejamentoDTO.fromEntity(termo);
        preencherValoresProdutoNoModal(dto, termo.getDemandaTecnica());
        return dto;
    }
    
    private void preencherValoresProdutoNoModal(TermoPlanejamentoDTO dto, DemandaTecnica demanda) {
        if (demanda == null || demanda.getMetaProduto() == null) return;
        var produto = demanda.getMetaProduto();
        if (produto.getValorUnitario() != null && produto.getQuantidade() != null) {
            dto.setValorPrevistoProduto(produto.getValorUnitario().multiply(BigDecimal.valueOf(produto.getQuantidade())));
        }
        BigDecimal valorExecutado = termoEncerramentoRepository.sumValorExecutadoByMetaProdutoIdAndStatus(produto.getId(), DemandaTecnica.STATUS_ENCERRADA);
        dto.setValorTotalExecutadoProduto(valorExecutado != null ? valorExecutado : BigDecimal.ZERO);
    }
    
    @Transactional
    public TermoPlanejamentoDTO create(TermoPlanejamentoCreateDTO dto) {
        DemandaTecnica demanda = demandaRepository.findById(dto.getDemandaTecnicaId())
            .orElseThrow(() -> new ResourceNotFoundException("Demanda não encontrada com ID: " + dto.getDemandaTecnicaId()));
        
        validarCriacaoPlanejamentoPermitida(demanda);
        
        if (termoRepository.findByDemandaTecnicaId(dto.getDemandaTecnicaId()).isPresent()) {
            throw new BadRequestException("Já existe termo de planejamento para esta demanda");
        }
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        TermoPlanejamento termo = new TermoPlanejamento();
        termo.setDemandaTecnica(demanda);
        termo.setDataAbertura(dto.getDataAbertura());
        termo.setDataInicioExecucao(dto.getDataInicioExecucao());
        termo.setDataFimExecucao(dto.getDataFimExecucao());
        termo.setEspecificacao(dto.getEspecificacao());
        termo.setCronograma(dto.getCronograma());
        termo.setResultadoEsperado(dto.getResultadoEsperado());
        termo.setUsuario(usuario);
        
        if (dto.getCustos() != null && !dto.getCustos().isEmpty()) {
            List<TermoPlanejamentoCusto> custos = new ArrayList<>();
            for (TermoPlanejamentoCustoCreateDTO custoDTO : dto.getCustos()) {
                Perfil perfil = perfilRepository.findById(custoDTO.getPerfilId())
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + custoDTO.getPerfilId()));
                
                TermoPlanejamentoCusto custo = new TermoPlanejamentoCusto();
                custo.setTermoPlanejamento(termo);
                custo.setPerfil(perfil);
                custo.setQtdeHora(custoDTO.getQtdeHora());
                custo.setValorHora(custoDTO.getValorHora());
                custos.add(custo);
            }
            termo.setCustos(custos);
        }
        
        TermoPlanejamento saved = termoRepository.save(termo);
        atualizarStatusDemandaAposSalvarPlanejamento(demanda);
        return TermoPlanejamentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public TermoPlanejamentoDTO update(Long id, TermoPlanejamentoUpdateDTO dto) {
        TermoPlanejamento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + id));
        
        DemandaTecnica demanda = termo.getDemandaTecnica();
        validarEdicaoPlanejamentoPermitida(demanda);
        
        if (dto.getDataAbertura() != null) {
            termo.setDataAbertura(dto.getDataAbertura());
        }
        if (dto.getDataInicioExecucao() != null) {
            termo.setDataInicioExecucao(dto.getDataInicioExecucao());
        }
        if (dto.getDataFimExecucao() != null) {
            termo.setDataFimExecucao(dto.getDataFimExecucao());
        }
        if (dto.getEspecificacao() != null) {
            termo.setEspecificacao(dto.getEspecificacao());
        }
        if (dto.getCronograma() != null) {
            termo.setCronograma(dto.getCronograma());
        }
        if (dto.getResultadoEsperado() != null) {
            termo.setResultadoEsperado(dto.getResultadoEsperado());
        }
        
        if (dto.getCustos() != null) {
            termoPlanejamentoCustoRepository.deleteByTermoPlanejamentoId(id);
            termoPlanejamentoCustoRepository.flush();
            List<TermoPlanejamentoCusto> novosCustos = new ArrayList<>();
            for (TermoPlanejamentoCustoCreateDTO custoDTO : dto.getCustos()) {
                Perfil perfil = perfilRepository.findById(custoDTO.getPerfilId())
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + custoDTO.getPerfilId()));
                TermoPlanejamentoCusto custo = new TermoPlanejamentoCusto();
                custo.setTermoPlanejamento(termo);
                custo.setPerfil(perfil);
                custo.setQtdeHora(custoDTO.getQtdeHora());
                custo.setValorHora(custoDTO.getValorHora());
                novosCustos.add(termoPlanejamentoCustoRepository.save(custo));
            }
            termo.setCustos(novosCustos);
        }
        
        TermoPlanejamento saved = termoRepository.save(termo);
        atualizarStatusDemandaAposSalvarPlanejamento(demanda);
        return TermoPlanejamentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public TermoPlanejamentoDTO sign(Long id) {
        TermoPlanejamento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + id));
        
        DemandaTecnica demanda = termo.getDemandaTecnica();
        validarFinalizacaoPlanejamentoPermitida(demanda, termo);
        
        termo.setDataAssinatura(LocalDateTime.now());
        TermoPlanejamento saved = termoRepository.save(termo);
       
        if (demanda != null) {
            demanda.setStatus(StatusDemandaTecnica.E.getCodigo());
            demandaRepository.save(demanda);
        }

        return TermoPlanejamentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public void delete(Long id) {
        TermoPlanejamento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + id));
        DemandaTecnica demanda = termo.getDemandaTecnica();
        validarEdicaoPlanejamentoPermitida(demanda);
        
        termoRepository.deleteById(id);
        
        if (demanda != null) {
            demanda.setStatus(resolverStatusDemandaAposExcluirPlanejamento(demanda.getId()));
            demandaRepository.save(demanda);
        }
    }

    /**
     * Valida se a demanda permite upload/alteração do PDF do termo de planejamento.
     */
    public void validarEnvioDocumentoPlanejamentoPermitida(Long termoPlanejamentoId, boolean atualizacao) {
        TermoPlanejamento termo = termoRepository.findById(termoPlanejamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + termoPlanejamentoId));
        DemandaTecnica demanda = termo.getDemandaTecnica();
        StatusDemandaTecnica status = resolverStatus(demanda);
        if (atualizacao && status == StatusDemandaTecnica.E) {
            return;
        }
        if (status == null || !status.permiteEnvioDocumentoPlanejamento()) {
            throw new BadRequestException(
                    "Não é permitido enviar o documento do Termo de Planejamento: a demanda deve estar com status "
                            + "'B', 'C', 'D' ou 'E'. Status atual: "
                            + (demanda != null ? demanda.getStatus() : "desconhecido") + ".");
        }
    }

    /** Valida exclusão do PDF (somente antes de ir para execução). */
    public void validarExclusaoDocumentoPlanejamentoPermitida(Long termoPlanejamentoId) {
        TermoPlanejamento termo = termoRepository.findById(termoPlanejamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + termoPlanejamentoId));
        validarEdicaoPlanejamentoPermitida(termo.getDemandaTecnica());
    }

    /**
     * Marca o planejamento como finalizado (PDF enviado) e move a demanda para execução (E).
     */
    @Transactional
    public void finalizarPlanejamentoComDocumento(Long termoPlanejamentoId) {
        TermoPlanejamento termo = termoRepository.findById(termoPlanejamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + termoPlanejamentoId));
        DemandaTecnica demanda = termo.getDemandaTecnica();
        validarFinalizacaoPlanejamentoPermitida(demanda, termo);
        if (demanda != null && !StatusDemandaTecnica.E.getCodigo().equals(demanda.getStatus())) {
            demanda.setStatus(StatusDemandaTecnica.E.getCodigo());
            demandaRepository.save(demanda);
        }
    }

    private void validarCriacaoPlanejamentoPermitida(DemandaTecnica demanda) {
        StatusDemandaTecnica status = resolverStatus(demanda);
        if (status == null || !status.permiteCriacaoTermoPlanejamento()) {
            throw new BadRequestException(mensagemEdicaoNaoPermitida(demanda, "criar o Termo de Planejamento"));
        }
    }

    private void validarEdicaoPlanejamentoPermitida(DemandaTecnica demanda) {
        StatusDemandaTecnica status = resolverStatus(demanda);
        if (status == null || !status.permiteEdicaoTermoPlanejamento()) {
            throw new BadRequestException(mensagemEdicaoNaoPermitida(demanda, "alterar o Termo de Planejamento"));
        }
    }

    private void validarFinalizacaoPlanejamentoPermitida(DemandaTecnica demanda, TermoPlanejamento termo) {
        StatusDemandaTecnica status = resolverStatus(demanda);
        if (status == null || !status.permiteFinalizacaoTermoPlanejamento()) {
            throw new BadRequestException(
                    "Não é permitido finalizar o Termo de Planejamento: a demanda deve estar com status "
                            + "'C' (Aberta e assinada), 'D' (Em planejamento) ou 'E' (Em execução). Status atual: "
                            + (demanda != null ? demanda.getStatus() : "desconhecido") + ".");
        }
        if (!isTermoAberturaAssinado(demanda.getId())) {
            throw new BadRequestException(
                    "Não é permitido assinar o Termo de Planejamento: o Termo de Abertura da demanda ainda não foi assinado.");
        }
        if (termo.getCustos() == null || termo.getCustos().isEmpty()) {
            throw new BadRequestException(
                    "Não é permitido assinar o Termo de Planejamento: informe ao menos um custo no planejamento.");
        }
    }

    private boolean isTermoAberturaAssinado(Long demandaId) {
        return termoAberturaRepository.findByDemandaTecnicaId(demandaId)
                .map(t -> t.getDataAssinatura() != null)
                .orElse(false);
    }

    private void atualizarStatusDemandaAposSalvarPlanejamento(DemandaTecnica demanda) {
        if (demanda == null) {
            return;
        }
        StatusDemandaTecnica atual = resolverStatus(demanda);
        if (atual != null && atual.permiteEdicaoTermoPlanejamento() && atual != StatusDemandaTecnica.D) {
            demanda.setStatus(StatusDemandaTecnica.D.getCodigo());
            demandaRepository.save(demanda);
        }
    }

    private String resolverStatusDemandaAposExcluirPlanejamento(Long demandaId) {
        if (isTermoAberturaAssinado(demandaId)) {
            return StatusDemandaTecnica.C.getCodigo();
        }
        return StatusDemandaTecnica.B.getCodigo();
    }

    private static StatusDemandaTecnica resolverStatus(DemandaTecnica demanda) {
        if (demanda == null || demanda.getStatus() == null) {
            return null;
        }
        return StatusDemandaTecnica.fromCodigo(demanda.getStatus());
    }

    private static String mensagemEdicaoNaoPermitida(DemandaTecnica demanda, String acao) {
        return "Não é permitido " + acao + ": a demanda deve estar com status 'B' (Em abertura), "
                + "'C' (Aberta e assinada) ou 'D' (Em planejamento). Status atual: "
                + (demanda != null ? demanda.getStatus() : "desconhecido") + ".";
    }
}
