package com.demandtracker.service;

import com.demandtracker.dto.ProfissionalAnaliseResumidaDTO;
import com.demandtracker.dto.ProfissionalCreateDTO;
import com.demandtracker.dto.ProfissionalDTO;
import com.demandtracker.dto.ProfissionalDemandaTecnicaDTO;
import com.demandtracker.dto.ProfissionalDemandaTecnicaMensalDTO;
import com.demandtracker.dto.ProfissionalDemandaTecnicaResumoMensalDTO;
import com.demandtracker.dto.ProfissionalDemandasTecnicasResponseDTO;
import com.demandtracker.dto.ProfissionalUpdateDTO;
import com.demandtracker.entity.DemandaExecucao;
import com.demandtracker.entity.DemandaExecucaoTarefa;
import com.demandtracker.entity.DemandaExecucaoTarefaRecurso;
import com.demandtracker.entity.DemandaTecnica;
import com.demandtracker.entity.Profissional;
import com.demandtracker.entity.ProfissionalCustoMensal;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.Perfil;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaExecucaoRepository;
import com.demandtracker.repository.DemandaExecucaoTarefaRecursoRepository;
import com.demandtracker.repository.DiaNaoUtilRepository;
import com.demandtracker.repository.ProfissionalCustoMensalRepository;
import com.demandtracker.repository.ProfissionalRepository;
import com.demandtracker.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final ProjetoRepository projetoRepository;
    private final com.demandtracker.repository.PerfilRepository perfilRepository;
    private final DemandaExecucaoRepository demandaExecucaoRepository;
    private final DemandaExecucaoTarefaRecursoRepository demandaExecucaoTarefaRecursoRepository;
    private final ProfissionalCustoMensalRepository profissionalCustoMensalRepository;
    private final DiaNaoUtilRepository diaNaoUtilRepository;
    private final DiaUtilService diaUtilService;

    @Transactional(readOnly = true)
    public Page<ProfissionalDTO> findAll(String nome, Long projetoId, Pageable pageable) {
        Page<Profissional> profissionais;

        if (nome != null && projetoId != null) {
            profissionais = profissionalRepository.findByNomeContainingIgnoreCaseAndProjetoId(nome, projetoId, pageable);
        } else if (nome != null) {
            profissionais = profissionalRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (projetoId != null) {
            profissionais = profissionalRepository.findByProjetoId(projetoId, pageable);
        } else {
            profissionais = profissionalRepository.findAll(pageable);
        }

        return profissionais.map(ProfissionalDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProfissionalDTO findById(Long id) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + id));
        return ProfissionalDTO.fromEntity(profissional);
    }

    @Transactional
    public ProfissionalDTO create(ProfissionalCreateDTO dto) {
        if (profissionalRepository.existsByDocumento(dto.getDocumento())) {
            throw new BadRequestException(
                    "Já existe um profissional cadastrado com o documento informado: " + dto.getDocumento());
        }

        Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));

        Perfil perfil = perfilRepository.findById(dto.getPerfilId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + dto.getPerfilId()));

        Profissional profissional = new Profissional();
        profissional.setNome(dto.getNome());
        profissional.setDataInicioAtividade(dto.getDataInicioAtividade());
        profissional.setTipoPessoa(dto.getTipoPessoa());
        profissional.setDocumento(dto.getDocumento());
        profissional.setValorHora(dto.getValorHora());
        profissional.setCustoTotalMensal(dto.getCustoTotalMensal());
        profissional.setFuncao(dto.getFuncaoProfissional());
        profissional.setProjeto(projeto);
        profissional.setPerfil(perfil);

        Profissional saved = profissionalRepository.save(profissional);
        return ProfissionalDTO.fromEntity(saved);
    }

    @Transactional
    public ProfissionalDTO update(Long id, ProfissionalUpdateDTO dto) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + id));

        if (dto.getDocumento() != null && !dto.getDocumento().equals(profissional.getDocumento())
                && profissionalRepository.existsByDocumentoAndIdNot(dto.getDocumento(), id)) {
            throw new BadRequestException(
                    "Já existe um profissional cadastrado com o documento informado: " + dto.getDocumento());
        }

        if (dto.getNome() != null) {
            profissional.setNome(dto.getNome());
        }
        if (dto.getDataInicioAtividade() != null) {
            profissional.setDataInicioAtividade(dto.getDataInicioAtividade());
        }
        if (dto.getValorHora() != null) {
            profissional.setValorHora(dto.getValorHora());
        }
        if (dto.getCustoTotalMensal() != null) {
            profissional.setCustoTotalMensal(dto.getCustoTotalMensal());
        }
        if (dto.getProjetoId() != null) {
            Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
            profissional.setProjeto(projeto);
        }
        if (dto.getTipoPessoa() != null) {
            profissional.setTipoPessoa(dto.getTipoPessoa());
        }
        if (dto.getDocumento() != null) {
            profissional.setDocumento(dto.getDocumento());
        }
        if (dto.getFuncao() != null) {
            profissional.setFuncao(dto.getFuncao());
        }
        if (dto.getPerfilId() != null) {
            Perfil perfil = perfilRepository.findById(dto.getPerfilId())
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + dto.getPerfilId()));
            profissional.setPerfil(perfil);
        }

        Profissional saved = profissionalRepository.save(profissional);
        return ProfissionalDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!profissionalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Profissional não encontrado com ID: " + id);
        }
        profissionalRepository.deleteById(id);
    }

    /**
     * Análise mensal resumida do profissional: horas executadas rateadas por dias úteis
     * (tarefa data_inicio_real/fim_real com fallback planejada), valor por perfil e custo mensal.
     *
     * @param demandaExecucaoId opcional; quando informado, considera apenas recursos da execução indicada
     */
    @Transactional(readOnly = true)
    public List<ProfissionalAnaliseResumidaDTO> getAnaliseResumida(Long profissionalId, Long demandaExecucaoId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + profissionalId));

        if (demandaExecucaoId != null) {
            if (!demandaExecucaoRepository.existsById(demandaExecucaoId)) {
                throw new ResourceNotFoundException("Execução da demanda não encontrada com ID: " + demandaExecucaoId);
            }
        }

        ProfissionalDTO profissionalDto = ProfissionalDTO.fromEntity(profissional);
        LocalDate inicioAtividade = profissional.getDataInicioAtividade();
        LocalDate hoje = LocalDate.now();
        YearMonth inicioYm = YearMonth.from(inicioAtividade);
        YearMonth fimYm = YearMonth.from(hoje);

        Map<YearMonth, ProfissionalCustoMensal> custoMensalPorMes = new HashMap<>();
        for (ProfissionalCustoMensal c : profissionalCustoMensalRepository.findByProfissionalId(profissionalId)) {
            YearMonth ym = YearMonth.of(c.getAno(), c.getMes());
            ProfissionalCustoMensal atual = custoMensalPorMes.get(ym);
            if (atual == null || (c.getId() != null && atual.getId() != null && c.getId() > atual.getId())) {
                custoMensalPorMes.put(ym, c);
            }
        }

        Map<YearMonth, BigDecimal> horasExecutadasPorMes = new HashMap<>();
        Map<YearMonth, BigDecimal> valorPerfilPorMes = new HashMap<>();

        List<DemandaExecucaoTarefaRecurso> recursos = demandaExecucaoId != null
                ? demandaExecucaoTarefaRecursoRepository.findByProfissionalIdAndDemandaExecucaoTarefa_DemandaExecucaoId(
                        profissionalId, demandaExecucaoId)
                : demandaExecucaoTarefaRecursoRepository.findByProfissionalId(profissionalId);
        LocalDate minInicio = null;
        LocalDate maxFim = null;

        for (DemandaExecucaoTarefaRecurso recurso : recursos) {
            BigDecimal horasExecutadas = safe(recurso.getHorasExecutadas());
            if (horasExecutadas.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            LocalDate periodoInicio = resolveInicioTarefa(recurso);
            LocalDate periodoFim = resolveFimTarefa(recurso);
            if (periodoInicio == null || periodoFim == null || periodoFim.isBefore(periodoInicio)) {
                continue;
            }

            if (minInicio == null || periodoInicio.isBefore(minInicio)) {
                minInicio = periodoInicio;
            }
            if (maxFim == null || periodoFim.isAfter(maxFim)) {
                maxFim = periodoFim;
            }
        }

        Set<LocalDate> diasNaoUtil = (minInicio != null && maxFim != null)
                ? diaNaoUtilRepository.findDatasBetween(minInicio, maxFim)
                : Set.of();

        for (DemandaExecucaoTarefaRecurso recurso : recursos) {
            BigDecimal horasExecutadas = safe(recurso.getHorasExecutadas());
            if (horasExecutadas.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            LocalDate periodoInicio = resolveInicioTarefa(recurso);
            LocalDate periodoFim = resolveFimTarefa(recurso);
            if (periodoInicio == null || periodoFim == null || periodoFim.isBefore(periodoInicio)) {
                continue;
            }

            Map<YearMonth, BigDecimal> horasPorMes = diaUtilService.calcularHorasPorMesPorDiasUteis(
                    horasExecutadas, periodoInicio, periodoFim, diasNaoUtil);
            BigDecimal valorPerfil = recurso.getPerfil() != null ? safe(recurso.getPerfil().getValor()) : BigDecimal.ZERO;

            for (Map.Entry<YearMonth, BigDecimal> e : horasPorMes.entrySet()) {
                YearMonth ym = e.getKey();
                BigDecimal horasMes = e.getValue();
                horasExecutadasPorMes.merge(ym, horasMes, BigDecimal::add);
                valorPerfilPorMes.merge(ym, horasMes.multiply(valorPerfil), BigDecimal::add);
            }
        }

        List<ProfissionalAnaliseResumidaDTO> resultado = new ArrayList<>();
        YearMonth cursor = inicioYm;
        while (!cursor.isAfter(fimYm)) {
            BigDecimal horasExec = safe(horasExecutadasPorMes.get(cursor)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal valorPerfil = safe(valorPerfilPorMes.get(cursor)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal valorCusto = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            ProfissionalCustoMensal custoMes = custoMensalPorMes.get(cursor);
            if (custoMes != null) {
                valorCusto = safe(custoMes.getCustoTotal()).setScale(2, RoundingMode.HALF_UP);
            }

            resultado.add(new ProfissionalAnaliseResumidaDTO(
                    profissionalDto,
                    cursor.getYear(),
                    cursor.getMonthValue(),
                    horasExec,
                    valorPerfil,
                    valorCusto
            ));
            cursor = cursor.plusMonths(1);
        }
        return resultado;
    }

    /**
     * Lista as demandas técnicas em que o profissional foi alocado (recursos de execução),
     * com totais de horas (planejadas e executadas) na DT, período das tarefas e rateio mensal
     * por capacidade (dias úteis × 8) com teto das horas lançadas, mais resumo mensal agregado
     * com custo de perfil e custo mensal lançado.
     */
    @Transactional(readOnly = true)
    public ProfissionalDemandasTecnicasResponseDTO getDemandasTecnicasAlocadas(Long profissionalId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + profissionalId));

        Map<YearMonth, ProfissionalCustoMensal> custoMensalPorMes = new HashMap<>();
        for (ProfissionalCustoMensal c : profissionalCustoMensalRepository.findByProfissionalId(profissionalId)) {
            YearMonth ym = YearMonth.of(c.getAno(), c.getMes());
            ProfissionalCustoMensal atual = custoMensalPorMes.get(ym);
            if (atual == null || (c.getId() != null && atual.getId() != null && c.getId() > atual.getId())) {
                custoMensalPorMes.put(ym, c);
            }
        }

        BigDecimal valorPerfil = (profissional.getPerfil() != null)
                ? safe(profissional.getPerfil().getValor())
                : BigDecimal.ZERO;

        List<DemandaExecucaoTarefaRecurso> recursos =
                demandaExecucaoTarefaRecursoRepository.findByProfissionalIdWithDemandaTecnica(profissionalId);

        LocalDate minInicio = null;
        LocalDate maxFim = null;
        for (DemandaExecucaoTarefaRecurso recurso : recursos) {
            LocalDate inicio = resolveInicioTarefa(recurso);
            LocalDate fim = resolveFimTarefa(recurso);
            if (inicio == null || fim == null || fim.isBefore(inicio)) {
                continue;
            }
            if (minInicio == null || inicio.isBefore(minInicio)) {
                minInicio = inicio;
            }
            if (maxFim == null || fim.isAfter(maxFim)) {
                maxFim = fim;
            }
        }

        Set<LocalDate> diasNaoUtil = Set.of();
        if (minInicio != null && maxFim != null) {
            LocalDate inicioMesCivil = YearMonth.from(minInicio).atDay(1);
            LocalDate fimMesCivil = YearMonth.from(maxFim).atEndOfMonth();
            diasNaoUtil = diaNaoUtilRepository.findDatasBetween(inicioMesCivil, fimMesCivil);
        }

        Map<Long, ProfissionalDemandaTecnicaDTO> porDemanda = new HashMap<>();
        Map<Long, Map<YearMonth, TotaisMensaisAcc>> mensalPorDemanda = new HashMap<>();

        for (DemandaExecucaoTarefaRecurso recurso : recursos) {
            DemandaExecucaoTarefa tarefa = recurso.getDemandaExecucaoTarefa();
            if (tarefa == null || tarefa.getDemandaExecucao() == null || tarefa.getDemandaExecucao().getDemanda() == null) {
                continue;
            }

            DemandaExecucao execucao = tarefa.getDemandaExecucao();
            DemandaTecnica demanda = execucao.getDemanda();
            Long demandaId = demanda.getId();

            ProfissionalDemandaTecnicaDTO dto = porDemanda.computeIfAbsent(demandaId, id -> {
                ProfissionalDemandaTecnicaDTO item = new ProfissionalDemandaTecnicaDTO();
                item.setDemandaTecnicaId(demanda.getId());
                item.setDemandaCodigo(demanda.getCodigo());
                item.setDemandaNome(demanda.getNome());
                item.setDemandaStatus(demanda.getStatus());
                item.setTotalHorasExecutadas(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                item.setTotalHorasPlanejadas(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                item.setTotalHorasUteisPeriodo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                item.setTotaisMensais(new ArrayList<>());
                item.setDataInicioExecucao(null);
                item.setDataFimExecucao(null);
                return item;
            });

            LocalDate inicioTarefa = resolveInicioTarefa(recurso);
            LocalDate fimTarefa = resolveFimTarefa(recurso);
            if (inicioTarefa != null
                    && (dto.getDataInicioExecucao() == null || inicioTarefa.isBefore(dto.getDataInicioExecucao()))) {
                dto.setDataInicioExecucao(inicioTarefa);
            }
            if (fimTarefa != null
                    && (dto.getDataFimExecucao() == null || fimTarefa.isAfter(dto.getDataFimExecucao()))) {
                dto.setDataFimExecucao(fimTarefa);
            }

            BigDecimal horasExecutadas = safe(recurso.getHorasExecutadas());
            BigDecimal horasPlanejadas = safe(recurso.getHorasPlanejadas());
            dto.setTotalHorasExecutadas(
                    dto.getTotalHorasExecutadas().add(horasExecutadas).setScale(2, RoundingMode.HALF_UP));
            dto.setTotalHorasPlanejadas(
                    dto.getTotalHorasPlanejadas().add(horasPlanejadas).setScale(2, RoundingMode.HALF_UP));

            if (inicioTarefa != null && fimTarefa != null && !fimTarefa.isBefore(inicioTarefa)) {
                Map<YearMonth, TotaisMensaisAcc> accMensal =
                        mensalPorDemanda.computeIfAbsent(demandaId, id -> new HashMap<>());

                Map<YearMonth, BigDecimal> rateioPlanejado = alocarHorasPorMesComTeto(
                        horasPlanejadas, inicioTarefa, fimTarefa, diasNaoUtil);
                Map<YearMonth, BigDecimal> rateioExecutado = alocarHorasPorMesComTeto(
                        horasExecutadas, inicioTarefa, fimTarefa, diasNaoUtil);

                for (Map.Entry<YearMonth, BigDecimal> e : rateioPlanejado.entrySet()) {
                    accMensal.computeIfAbsent(e.getKey(), ym -> new TotaisMensaisAcc())
                            .addPlanejado(e.getValue());
                }
                for (Map.Entry<YearMonth, BigDecimal> e : rateioExecutado.entrySet()) {
                    accMensal.computeIfAbsent(e.getKey(), ym -> new TotaisMensaisAcc())
                            .addExecutado(e.getValue());
                }
            }
        }

        for (Map.Entry<Long, ProfissionalDemandaTecnicaDTO> entry : porDemanda.entrySet()) {
            ProfissionalDemandaTecnicaDTO item = entry.getValue();
            int diasUteis = diaUtilService.contarDiasUteis(
                    item.getDataInicioExecucao(), item.getDataFimExecucao(), diasNaoUtil);
            item.setTotalHorasUteisPeriodo(
                    BigDecimal.valueOf(diasUteis)
                            .multiply(BigDecimal.valueOf(DiaUtilService.HORAS_POR_DIA_UTIL))
                            .setScale(2, RoundingMode.HALF_UP));

            Map<YearMonth, TotaisMensaisAcc> accMensal =
                    mensalPorDemanda.getOrDefault(entry.getKey(), Map.of());
            List<ProfissionalDemandaTecnicaMensalDTO> totaisMensais = new ArrayList<>();
            accMensal.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> {
                        YearMonth ym = e.getKey();
                        TotaisMensaisAcc acc = e.getValue();
                        totaisMensais.add(new ProfissionalDemandaTecnicaMensalDTO(
                                ym.getYear(),
                                ym.getMonthValue(),
                                acc.totalPlanejado.setScale(2, RoundingMode.HALF_UP),
                                acc.totalExecutado.setScale(2, RoundingMode.HALF_UP)
                        ));
                    });
            item.setTotaisMensais(totaisMensais);
        }

        List<ProfissionalDemandaTecnicaDTO> demandasTecnicas = new ArrayList<>(porDemanda.values());
        demandasTecnicas.sort(Comparator.comparing(
                ProfissionalDemandaTecnicaDTO::getDemandaCodigo,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

        List<ProfissionalDemandaTecnicaResumoMensalDTO> resumoMensal =
                montarResumoMensal(
                        demandasTecnicas,
                        valorPerfil,
                        custoMensalPorMes,
                        diasNaoUtil,
                        profissional.getDataInicioAtividade());

        return new ProfissionalDemandasTecnicasResponseDTO(demandasTecnicas, resumoMensal);
    }

    /**
     * Agrega {@code totaisMensais} de todas as DTs e aplica custos de perfil e mensal lançado.
     * {@code horasPrevistas} = dias úteis do mês × 8 a partir de {@code max(1º do mês, dataInicioAtividade)}.
     */
    List<ProfissionalDemandaTecnicaResumoMensalDTO> montarResumoMensal(
            List<ProfissionalDemandaTecnicaDTO> demandasTecnicas,
            BigDecimal valorPerfil,
            Map<YearMonth, ProfissionalCustoMensal> custoMensalPorMes,
            Set<LocalDate> diasNaoUtil,
            LocalDate dataInicioAtividade) {
        Map<YearMonth, TotaisMensaisAcc> agregado = new HashMap<>();
        if (demandasTecnicas != null) {
            for (ProfissionalDemandaTecnicaDTO dt : demandasTecnicas) {
                if (dt.getTotaisMensais() == null) {
                    continue;
                }
                for (ProfissionalDemandaTecnicaMensalDTO m : dt.getTotaisMensais()) {
                    if (m.getAno() == null || m.getMes() == null) {
                        continue;
                    }
                    YearMonth ym = YearMonth.of(m.getAno(), m.getMes());
                    TotaisMensaisAcc acc = agregado.computeIfAbsent(ym, k -> new TotaisMensaisAcc());
                    acc.addPlanejado(m.getTotalPlanejado());
                    acc.addExecutado(m.getTotalExecutado());
                }
            }
        }

        BigDecimal valorHoraPerfil = safe(valorPerfil);
        Map<YearMonth, ProfissionalCustoMensal> custos =
                custoMensalPorMes != null ? custoMensalPorMes : Map.of();
        Set<LocalDate> naoUteis = diasNaoUtil != null ? diasNaoUtil : Set.of();

        List<ProfissionalDemandaTecnicaResumoMensalDTO> resumo = new ArrayList<>();
        agregado.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    YearMonth ym = e.getKey();
                    TotaisMensaisAcc acc = e.getValue();
                    BigDecimal totalPlanejado = acc.totalPlanejado.setScale(2, RoundingMode.HALF_UP);
                    BigDecimal totalExecutado = acc.totalExecutado.setScale(2, RoundingMode.HALF_UP);
                    BigDecimal valorCustoPerfil = totalExecutado
                            .multiply(valorHoraPerfil)
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal valorCustoMensal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    ProfissionalCustoMensal custoMes = custos.get(ym);
                    if (custoMes != null) {
                        valorCustoMensal = safe(custoMes.getCustoTotal()).setScale(2, RoundingMode.HALF_UP);
                    }
                    BigDecimal horasPrevistas = diaUtilService.calcularHorasPrevistasNoMes(
                            ym, dataInicioAtividade, naoUteis);
                    resumo.add(new ProfissionalDemandaTecnicaResumoMensalDTO(
                            ym.getYear(),
                            ym.getMonthValue(),
                            totalPlanejado,
                            totalExecutado,
                            valorCustoPerfil,
                            valorCustoMensal,
                            horasPrevistas
                    ));
                });
        return resumo;
    }

    /**
     * Aloca {@code totalHoras} por mês: capacidade = dias úteis × 8, consumindo o total
     * em ordem cronológica (teto sequencial). Horas &lt;= 0 → mapa vazio.
     */
    Map<YearMonth, BigDecimal> alocarHorasPorMesComTeto(
            BigDecimal totalHoras,
            LocalDate inicio,
            LocalDate fim,
            Set<LocalDate> diasNaoUtil) {
        BigDecimal total = safe(totalHoras).setScale(2, RoundingMode.HALF_UP);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return Map.of();
        }
        return diaUtilService.calcularHorasPorMesPorDiasUteis(total, inicio, fim, diasNaoUtil);
    }

    Map<YearMonth, Integer> contarDiasUteisPorMes(LocalDate inicio, LocalDate fim, Set<LocalDate> diasNaoUtil) {
        Map<YearMonth, Integer> result = new HashMap<>();
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            return result;
        }
        YearMonth ymInicio = YearMonth.from(inicio);
        YearMonth ymFim = YearMonth.from(fim);
        YearMonth cursor = ymInicio;
        while (!cursor.isAfter(ymFim)) {
            LocalDate iniMes = cursor.equals(ymInicio) ? inicio : cursor.atDay(1);
            LocalDate fimMes = cursor.equals(ymFim) ? fim : cursor.atEndOfMonth();
            int dias = diaUtilService.contarDiasUteis(iniMes, fimMes, diasNaoUtil);
            if (dias > 0) {
                result.put(cursor, dias);
            }
            cursor = cursor.plusMonths(1);
        }
        return result;
    }

    private static final class TotaisMensaisAcc {
        private BigDecimal totalPlanejado = BigDecimal.ZERO;
        private BigDecimal totalExecutado = BigDecimal.ZERO;

        void addPlanejado(BigDecimal valor) {
            totalPlanejado = totalPlanejado.add(safe(valor));
        }

        void addExecutado(BigDecimal valor) {
            totalExecutado = totalExecutado.add(safe(valor));
        }
    }

    private static LocalDate resolveInicioTarefa(DemandaExecucaoTarefaRecurso recurso) {
        DemandaExecucaoTarefa tarefa = recurso.getDemandaExecucaoTarefa();
        if (tarefa == null) {
            return null;
        }
        return tarefa.getDataInicioReal() != null ? tarefa.getDataInicioReal() : tarefa.getDataInicioPlanejada();
    }

    private static LocalDate resolveFimTarefa(DemandaExecucaoTarefaRecurso recurso) {
        DemandaExecucaoTarefa tarefa = recurso.getDemandaExecucaoTarefa();
        if (tarefa == null) {
            return null;
        }
        return tarefa.getDataFimReal() != null ? tarefa.getDataFimReal() : tarefa.getDataFimPlanejada();
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
