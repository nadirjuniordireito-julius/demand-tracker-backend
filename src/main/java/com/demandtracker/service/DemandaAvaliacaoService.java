package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.*;
import com.demandtracker.entity.enums.TipoRisco;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.BusinessException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaAvaliacaoRepository;
import com.demandtracker.repository.DemandaTecnicaRepository;
import com.demandtracker.repository.UsuarioRepository;
import com.demandtracker.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandaAvaliacaoService {

    private static final int TOTAL_TIPOS_RISCO = TipoRisco.values().length;
    private static final int SCALE = 4;

    private final DemandaAvaliacaoRepository avaliacaoRepository;
    private final DemandaTecnicaRepository demandaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public DemandaAvaliacaoResponseDTO findByDemandaId(Long demandaId) {
        DemandaAvaliacao a = avaliacaoRepository.findByDemandaIdWithRiscosETexto(demandaId)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada para demanda: " + demandaId));
        return DemandaAvaliacaoResponseDTO.fromEntity(a);
    }

    @Transactional
    public DemandaAvaliacaoResponseDTO create(Long demandaId, DemandaAvaliacaoRequestDTO dto) {
        DemandaTecnica demanda = demandaRepository.findById(demandaId)
                .orElseThrow(() -> new ResourceNotFoundException("Demanda não encontrada com ID: " + demandaId));
        if (!DemandaTecnica.STATUS_ENCERRADA.equals(demanda.getStatus())) {
            throw new BusinessException("Demanda ainda não encerrada");
        }
        if (demanda.getTermoEncerramento() == null) {
            throw new BusinessException("Termo de encerramento inexistente");
        }
        if (avaliacaoRepository.findByDemandaId(demandaId).isPresent()) {
            throw new BadRequestException("Já existe avaliação para esta demanda.");
        }
        DemandaAvaliacao a = mapRequestToEntity(dto, new DemandaAvaliacao(), demanda);
        a.setDemanda(demanda);
        a.setTermoEncerramentoId(demanda.getTermoEncerramento().getId());
        a.setDataEncerramento(dataEncerramentoFromTermo(demanda.getTermoEncerramento()));
        a.setDataAvaliacao(LocalDateTime.now());
        setUsuarioLogado(a);
        persistRiscosETexto(a, dto);
        computeIndices(a);
        DemandaAvaliacao saved = avaliacaoRepository.save(a);
        return DemandaAvaliacaoResponseDTO.fromEntity(saved);
    }

    @Transactional
    public DemandaAvaliacaoResponseDTO update(Long demandaId, DemandaAvaliacaoRequestDTO dto) {
        DemandaAvaliacao a = avaliacaoRepository.findByDemandaIdWithRiscosETexto(demandaId)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada para demanda: " + demandaId));
        mapRequestToEntity(dto, a, a.getDemanda());
        if (a.getDemanda().getTermoEncerramento() != null) {
            a.setDataEncerramento(dataEncerramentoFromTermo(a.getDemanda().getTermoEncerramento()));
        }
        setUsuarioLogado(a);
        if (dto.getRiscos() != null) {
            a.getRiscos().clear();
        }
        persistRiscosETexto(a, dto);
        computeIndices(a);
        DemandaAvaliacao saved = avaliacaoRepository.save(a);
        return DemandaAvaliacaoResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public DemandaAvaliacaoKPIsDTO getKpis(Long demandaId) {
        DemandaAvaliacao a = avaliacaoRepository.findByDemandaId(demandaId)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada para demanda: " + demandaId));
        return new DemandaAvaliacaoKPIsDTO(a.getIndiceSaude(), a.getIndiceConfiabilidade(), a.getIndiceRisco());
    }

    @Transactional(readOnly = true)
    public List<TipoRisco> getRiscos(Long demandaId) {
        DemandaAvaliacao a = avaliacaoRepository.findByDemandaIdWithRiscosETexto(demandaId)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada para demanda: " + demandaId));
        return a.getRiscos().stream().map(DemandaAvaliacaoRisco::getTipo).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DemandaAvaliacaoAnalyticsDTO getAnalytics() {
        List<DemandaAvaliacao> todas = avaliacaoRepository.findAllWithDemandaAndProjetoAndMeta();
        Map<Long, List<DemandaAvaliacao>> porProjeto = new LinkedHashMap<>();
        Map<Long, List<DemandaAvaliacao>> porMeta = new LinkedHashMap<>();
        Map<Long, List<DemandaAvaliacao>> porProduto = new LinkedHashMap<>();

        for (DemandaAvaliacao a : todas) {
            DemandaTecnica d = a.getDemanda();
            if (d.getProjeto() != null) {
                porProjeto.computeIfAbsent(d.getProjeto().getId(), k -> new ArrayList<>()).add(a);
            }
            if (d.getMetaProduto() != null) {
                porProduto.computeIfAbsent(d.getMetaProduto().getId(), k -> new ArrayList<>()).add(a);
                if (d.getMetaProduto().getProjetoMeta() != null) {
                    porMeta.computeIfAbsent(d.getMetaProduto().getProjetoMeta().getId(), k -> new ArrayList<>()).add(a);
                }
            }
        }

        List<DemandaAvaliacaoAnalyticsDTO.AgregadoDTO> agregadosProjeto = toAgregados(porProjeto, this::projetoIdNomeCodigo);
        List<DemandaAvaliacaoAnalyticsDTO.AgregadoDTO> agregadosMeta = toAgregados(porMeta, this::metaIdNomeCodigo);
        List<DemandaAvaliacaoAnalyticsDTO.AgregadoDTO> agregadosProduto = toAgregados(porProduto, this::produtoIdNomeCodigo);

        return new DemandaAvaliacaoAnalyticsDTO(agregadosProduto, agregadosMeta, agregadosProjeto);
    }

    /** Define o usuário logado e data/hora de preenchimento (esta é atualizada em @PreUpdate na entidade). */
    private void setUsuarioLogado(DemandaAvaliacao a) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            usuarioRepository.findByEmail(auth.getName()).ifPresent(a::setUsuario);
        }
    }

    /** Data de encerramento conforme o termo de encerramento da demanda (data fim execução ou data do termo). */
    private static LocalDate dataEncerramentoFromTermo(TermoEncerramento termo) {
        if (termo == null) return null;
        if (termo.getDataFimExecucao() != null) return termo.getDataFimExecucao();
        return termo.getDataTermo() != null ? termo.getDataTermo().toLocalDate() : null;
    }

    private DemandaAvaliacao mapRequestToEntity(DemandaAvaliacaoRequestDTO dto, DemandaAvaliacao a, DemandaTecnica demanda) {
        // dataEncerramento é preenchida no create/update a partir do termo de encerramento
        a.setAtraso(dto.getAtraso());
        if (Boolean.FALSE.equals(dto.getAtraso())) {
            a.setDesvioPrazoPercentual(BigDecimal.ZERO);
            a.setImpactoAtraso(0);
        } else {
            a.setDesvioPrazoPercentual(dto.getDesvioPrazoPercentual());
            a.setImpactoAtraso(dto.getImpactoAtraso());
        }
        a.setDesvioCustoPercentual(dto.getDesvioCustoPercentual());
        a.setImpactoFinanceiro(dto.getImpactoFinanceiro());
        a.setAtendimentoRequisitos(dto.getAtendimentoRequisitos());
        a.setEstabilidade(dto.getEstabilidade());
        a.setRetrabalho(dto.getRetrabalho());
        a.setSatisfacaoUsuario(dto.getSatisfacaoUsuario());
        a.setClarezaRequisitos(dto.getClarezaRequisitos());
        a.setQualidadePlanejamento(dto.getQualidadePlanejamento());
        a.setAderenciaCronograma(dto.getAderenciaCronograma());
        a.setComunicacao(dto.getComunicacao());
        a.setCapacidadeEquipe(dto.getCapacidadeEquipe());
        a.setDisponibilidadeEquipe(dto.getDisponibilidadeEquipe());
        a.setPossuiBackupCritico(dto.getPossuiBackupCritico());
        a.setRotatividadeImpactou(dto.getRotatividadeImpactou());
        a.setValorPercebido(dto.getValorPercebido());
        a.setAlinhamentoMeta(dto.getAlinhamentoMeta());
        a.setReutilizacao(dto.getReutilizacao());
        a.setAvaliacaoGeral(dto.getAvaliacaoGeral());
        a.setRepetiriaModelo(dto.getRepetiriaModelo());
        return a;
    }

    private void computeIndices(DemandaAvaliacao a) {
        BigDecimal prazo = bigDecimal(a.getAderenciaCronograma() != null ? a.getAderenciaCronograma() : 5);
        BigDecimal custo = a.getImpactoFinanceiro() != null ? BigDecimal.valueOf(6 - a.getImpactoFinanceiro()) : BigDecimal.valueOf(5);
        BigDecimal qualidade = media(
                a.getAtendimentoRequisitos(),
                a.getEstabilidade(),
                a.getQualidadePlanejamento(),
                a.getClarezaRequisitos()
        );
        BigDecimal maturidade = media(
                a.getCapacidadeEquipe(),
                a.getDisponibilidadeEquipe(),
                a.getComunicacao(),
                a.getValorPercebido(),
                a.getAlinhamentoMeta()
        );
        a.setIndiceSaude(prazo.multiply(BigDecimal.valueOf(0.2))
                .add(custo.multiply(BigDecimal.valueOf(0.2)))
                .add(qualidade.multiply(BigDecimal.valueOf(0.3)))
                .add(maturidade.multiply(BigDecimal.valueOf(0.3)))
                .setScale(SCALE, RoundingMode.HALF_UP));

        a.setIndiceConfiabilidade(media(a.getCapacidadeEquipe(), a.getDisponibilidadeEquipe(), a.getComunicacao()).setScale(SCALE, RoundingMode.HALF_UP));

        int qtdRiscos = a.getRiscos() != null ? a.getRiscos().size() : 0;
        a.setIndiceRisco(BigDecimal.valueOf(qtdRiscos).divide(BigDecimal.valueOf(TOTAL_TIPOS_RISCO), SCALE, RoundingMode.HALF_UP));
    }

    private void persistRiscosETexto(DemandaAvaliacao a, DemandaAvaliacaoRequestDTO dto) {
        if (dto.getRiscos() != null) {
            for (TipoRisco t : dto.getRiscos()) {
                DemandaAvaliacaoRisco r = new DemandaAvaliacaoRisco();
                r.setAvaliacao(a);
                r.setTipo(t);
                a.getRiscos().add(r);
            }
        }
        if (dto.getTexto() != null) {
            DemandaAvaliacaoTextoRequestDTO req = dto.getTexto();
            DemandaAvaliacaoTexto txt = a.getTexto();
            int maxTextLen = 10_000;
            if (txt != null) {
                txt.setCausaAtraso(InputSanitizer.trimAndMaxLength(req.getCausaAtraso(), maxTextLen));
                txt.setCausaCusto(InputSanitizer.trimAndMaxLength(req.getCausaCusto(), maxTextLen));
                txt.setGargalo(InputSanitizer.trimAndMaxLength(req.getGargalo(), maxTextLen));
                txt.setImpactoEquipe(InputSanitizer.trimAndMaxLength(req.getImpactoEquipe(), maxTextLen));
                txt.setCorrecoes(InputSanitizer.trimAndMaxLength(req.getCorrecoes(), maxTextLen));
                txt.setLicoesPositivas(InputSanitizer.trimAndMaxLength(req.getLicoesPositivas(), maxTextLen));
                txt.setLicoesNegativas(InputSanitizer.trimAndMaxLength(req.getLicoesNegativas(), maxTextLen));
                txt.setMelhorias(InputSanitizer.trimAndMaxLength(req.getMelhorias(), maxTextLen));
            } else {
                txt = new DemandaAvaliacaoTexto();
                txt.setAvaliacao(a);
                txt.setCausaAtraso(InputSanitizer.trimAndMaxLength(req.getCausaAtraso(), maxTextLen));
                txt.setCausaCusto(InputSanitizer.trimAndMaxLength(req.getCausaCusto(), maxTextLen));
                txt.setGargalo(InputSanitizer.trimAndMaxLength(req.getGargalo(), maxTextLen));
                txt.setImpactoEquipe(InputSanitizer.trimAndMaxLength(req.getImpactoEquipe(), maxTextLen));
                txt.setCorrecoes(InputSanitizer.trimAndMaxLength(req.getCorrecoes(), maxTextLen));
                txt.setLicoesPositivas(InputSanitizer.trimAndMaxLength(req.getLicoesPositivas(), maxTextLen));
                txt.setLicoesNegativas(InputSanitizer.trimAndMaxLength(req.getLicoesNegativas(), maxTextLen));
                txt.setMelhorias(InputSanitizer.trimAndMaxLength(req.getMelhorias(), maxTextLen));
                a.setTexto(txt);
            }
        }
    }

    private static BigDecimal bigDecimal(int v) {
        return BigDecimal.valueOf(v);
    }

    private static BigDecimal media(Integer... values) {
        List<Integer> list = Arrays.stream(values).filter(Objects::nonNull).collect(Collectors.toList());
        if (list.isEmpty()) return BigDecimal.valueOf(5);
        double sum = list.stream().mapToInt(Integer::intValue).sum();
        return BigDecimal.valueOf(sum / (double) list.size()).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private List<DemandaAvaliacaoAnalyticsDTO.AgregadoDTO> toAgregados(
            Map<Long, List<DemandaAvaliacao>> map,
            java.util.function.Function<DemandaAvaliacao, DemandaAvaliacaoAnalyticsDTO.AgregadoDTO> idNomeCodigo) {
        return map.entrySet().stream()
                .map(e -> {
                    List<DemandaAvaliacao> list = e.getValue();
                    DemandaAvaliacaoAnalyticsDTO.AgregadoDTO base = idNomeCodigo.apply(list.get(0));
                    BigDecimal mediaSaude = list.stream().map(DemandaAvaliacao::getIndiceSaude).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(list.size()), SCALE, RoundingMode.HALF_UP);
                    BigDecimal mediaConf = list.stream().map(DemandaAvaliacao::getIndiceConfiabilidade).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(list.size()), SCALE, RoundingMode.HALF_UP);
                    BigDecimal mediaRisco = list.stream().map(DemandaAvaliacao::getIndiceRisco).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(list.size()), SCALE, RoundingMode.HALF_UP);
                    return new DemandaAvaliacaoAnalyticsDTO.AgregadoDTO(base.getId(), base.getCodigo(), base.getNome(), mediaSaude, mediaConf, mediaRisco, (long) list.size());
                })
                .collect(Collectors.toList());
    }

    private DemandaAvaliacaoAnalyticsDTO.AgregadoDTO projetoIdNomeCodigo(DemandaAvaliacao a) {
        Projeto p = a.getDemanda().getProjeto();
        return new DemandaAvaliacaoAnalyticsDTO.AgregadoDTO(p.getId(), p.getCodTed(), p.getNome(), null, null, null, null);
    }

    private DemandaAvaliacaoAnalyticsDTO.AgregadoDTO metaIdNomeCodigo(DemandaAvaliacao a) {
        ProjetoMeta m = a.getDemanda().getMetaProduto().getProjetoMeta();
        return new DemandaAvaliacaoAnalyticsDTO.AgregadoDTO(m.getId(), m.getCodigo(), m.getNome(), null, null, null, null);
    }

    private DemandaAvaliacaoAnalyticsDTO.AgregadoDTO produtoIdNomeCodigo(DemandaAvaliacao a) {
        MetaProduto mp = a.getDemanda().getMetaProduto();
        return new DemandaAvaliacaoAnalyticsDTO.AgregadoDTO(mp.getId(), mp.getCodigo(), mp.getNome(), null, null, null, null);
    }
}
