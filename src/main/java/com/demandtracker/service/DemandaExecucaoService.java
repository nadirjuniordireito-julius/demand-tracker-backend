package com.demandtracker.service;

import com.demandtracker.dto.DemandaExecucaoCreateDTO;
import com.demandtracker.dto.DemandaExecucaoDTO;
import com.demandtracker.dto.DemandaExecucaoGanttDTO;
import com.demandtracker.dto.DemandaExecucaoGanttRecursoDTO;
import com.demandtracker.dto.DemandaExecucaoGanttTarefaDTO;
import com.demandtracker.dto.DemandaExecucaoPerfilCheckDTO;
import com.demandtracker.dto.DemandaExecucaoUpdateDTO;
import com.demandtracker.dto.ExecucaoProfissionalDTO;
import com.demandtracker.dto.TermoEncerramentoCreateDTO;
import com.demandtracker.dto.TermoEncerramentoCustoCreateDTO;
import com.demandtracker.dto.TermoEncerramentoCustoProfissionalItemDTO;
import com.demandtracker.dto.TermoEncerramentoDTO;
import com.demandtracker.dto.TermoEncerramentoUpdateDTO;
import com.demandtracker.entity.DemandaExecucao;
import com.demandtracker.entity.DemandaExecucaoTarefa;
import com.demandtracker.entity.DemandaExecucaoTarefaDependencia;
import com.demandtracker.entity.DemandaExecucaoTarefaRecurso;
import com.demandtracker.entity.Profissional;
import com.demandtracker.entity.ProfissionalCustoMensal;
import com.demandtracker.entity.TermoPlanejamento;
import com.demandtracker.entity.DemandaTecnica;
import com.demandtracker.entity.Usuario;
import com.demandtracker.entity.enums.StatusDemandaTecnica;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaExecucaoRepository;
import com.demandtracker.repository.DemandaExecucaoTarefaDependenciaRepository;
import com.demandtracker.repository.DemandaExecucaoTarefaRecursoRepository;
import com.demandtracker.repository.DemandaTecnicaRepository;
import com.demandtracker.repository.ProfissionalCustoMensalRepository;
import com.demandtracker.repository.ProfissionalRepository;
import com.demandtracker.repository.TermoPlanejamentoRepository;
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DemandaExecucaoService {

    private final DemandaExecucaoRepository repository;
    private final DemandaTecnicaRepository demandaTecnicaRepository;
    private final DemandaExecucaoTarefaDependenciaRepository dependenciaRepository;
    private final DemandaExecucaoTarefaRecursoRepository recursoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ProfissionalCustoMensalRepository profissionalCustoMensalRepository;
    private final TermoPlanejamentoRepository termoPlanejamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final com.demandtracker.service.TermoEncerramentoService termoEncerramentoService;

    @Transactional(readOnly = true)
    public Page<DemandaExecucaoDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(DemandaExecucaoDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public DemandaExecucaoDTO findById(Long id) {
        DemandaExecucao e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Execução da demanda não encontrada com ID: " + id));
        return DemandaExecucaoDTO.fromEntity(e);
    }

    @Transactional(readOnly = true)
    public DemandaExecucaoDTO findByDemandaTecnicaId(Long demandaTecnicaId) {
        return repository.findByDemandaId(demandaTecnicaId)
                .map(DemandaExecucaoDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Execução não encontrada para a demanda técnica ID: " + demandaTecnicaId));
    }

    /**
     * Retorna dados da execução no formato para o frontend montar o Gantt.
     * Parâmetro: ID da DemandaTecnica.
     */
    @Transactional(readOnly = true)
    public DemandaExecucaoGanttDTO getGanttByDemandaTecnicaId(Long demandaTecnicaId) {
        DemandaExecucao execucao = repository.findByDemandaId(demandaTecnicaId)
                .orElseThrow(() -> new ResourceNotFoundException("Execução não encontrada para a demanda técnica ID: " + demandaTecnicaId));

        DemandaTecnica demanda = execucao.getDemanda();
        DemandaExecucaoGanttDTO gantt = new DemandaExecucaoGanttDTO();
        gantt.setDemandaTecnicaId(demanda.getId());
        gantt.setDemandaTecnicaCodigo(demanda.getCodigo());
        gantt.setDemandaTecnicaNome(demanda.getNome());
        gantt.setDemandaExecucaoId(execucao.getId());
        gantt.setDataInicioPlanejada(execucao.getDataInicioPlanejada());
        gantt.setDataFimPlanejada(execucao.getDataFimPlanejada());
        gantt.setDataInicioReal(execucao.getDataInicioReal());
        gantt.setDataFimReal(execucao.getDataFimReal());
        gantt.setStatus(execucao.getStatus());
        gantt.setPercentualProgresso(execucao.getPercentualProgresso());

        java.util.List<DemandaExecucaoGanttTarefaDTO> tarefasDto = java.util.stream.Stream.ofNullable(execucao.getTarefas())
                .flatMap(java.util.List::stream)
                .map(t -> toGanttTarefaDTO(t))
                .toList();
        gantt.setTarefas(tarefasDto);
        return gantt;
    }

    @Transactional(readOnly = true)
    public List<ExecucaoProfissionalDTO> getExecucaoProfissional(Long profissionalId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + profissionalId));

        LocalDate inicio = profissional.getDataInicioAtividade();
        LocalDate hoje = LocalDate.now();
        YearMonth inicioYm = YearMonth.from(inicio);
        YearMonth fimYm = YearMonth.from(hoje);

        // Fonte oficial da remuneração mensal: tabela profissional_custo_mensal por (profissionalId, ano, mes).
        // Se houver duplicidade indevida no mesmo mês, usa o registro mais recente (maior id).
        Map<YearMonth, ProfissionalCustoMensal> custoMensalPorMes = new HashMap<>();
        for (ProfissionalCustoMensal c : profissionalCustoMensalRepository.findByProfissionalId(profissionalId)) {
            YearMonth ym = YearMonth.of(c.getAno(), c.getMes());
            ProfissionalCustoMensal atual = custoMensalPorMes.get(ym);
            if (atual == null || (c.getId() != null && atual.getId() != null && c.getId() > atual.getId())) {
                custoMensalPorMes.put(ym, c);
            }
        }

        Map<YearMonth, BigDecimal> horasExecutadasPorMes = new HashMap<>();
        Map<YearMonth, BigDecimal> valorExecucaoPorMes = new HashMap<>();

        List<DemandaExecucaoTarefaRecurso> recursos = recursoRepository.findByProfissionalId(profissionalId);
        for (DemandaExecucaoTarefaRecurso recurso : recursos) {
            BigDecimal horasExecutadas = safe(recurso.getHorasExecutadas());
            if (horasExecutadas.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            LocalDate periodoInicio = resolveInicioExecucao(recurso);
            LocalDate periodoFim = resolveFimExecucao(recurso);
            if (periodoInicio == null || periodoFim == null || periodoFim.isBefore(periodoInicio)) {
                continue;
            }

            Map<YearMonth, BigDecimal> rateioHoras = ratearHorasPorDiasUteis(horasExecutadas, periodoInicio, periodoFim);
            BigDecimal valorPerfil = recurso.getPerfil() != null ? safe(recurso.getPerfil().getValor()) : BigDecimal.ZERO;

            for (Map.Entry<YearMonth, BigDecimal> e : rateioHoras.entrySet()) {
                YearMonth ym = e.getKey();
                BigDecimal horasMes = e.getValue();
                horasExecutadasPorMes.merge(ym, horasMes, BigDecimal::add);
                valorExecucaoPorMes.merge(ym, horasMes.multiply(valorPerfil), BigDecimal::add);
            }
        }

        List<ExecucaoProfissionalDTO> out = new ArrayList<>();
        YearMonth cursor = inicioYm;
        while (!cursor.isAfter(fimYm)) {
            LocalDate limiteInicio = cursor.equals(inicioYm) ? inicio : cursor.atDay(1);
            LocalDate limiteFim = cursor.equals(fimYm) ? hoje : cursor.atEndOfMonth();
            int diasUteis = contarDiasUteisNoIntervalo(limiteInicio, limiteFim);
            BigDecimal horasPrevistas = BigDecimal.valueOf(diasUteis).multiply(BigDecimal.valueOf(8)).setScale(2, RoundingMode.HALF_UP);

            BigDecimal horasExec = safe(horasExecutadasPorMes.get(cursor)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal remuneracao = BigDecimal.ZERO;
            ProfissionalCustoMensal custoMes = custoMensalPorMes.get(cursor);
            if (custoMes != null) {
                remuneracao = safe(custoMes.getCustoTotal());
            }
            remuneracao = remuneracao.setScale(2, RoundingMode.HALF_UP);
            BigDecimal valorExecucao = safe(valorExecucaoPorMes.get(cursor)).setScale(2, RoundingMode.HALF_UP);

            out.add(new ExecucaoProfissionalDTO(
                    profissionalId,
                    cursor.getMonthValue(),
                    cursor.getYear(),
                    horasPrevistas,
                    horasExec,
                    remuneracao,
                    valorExecucao
            ));
            cursor = cursor.plusMonths(1);
        }
        return out;
    }

    private DemandaExecucaoGanttTarefaDTO toGanttTarefaDTO(DemandaExecucaoTarefa t) {
        DemandaExecucaoGanttTarefaDTO dto = new DemandaExecucaoGanttTarefaDTO();
        dto.setId(t.getId());
        dto.setTitulo(t.getTitulo());
        dto.setDescricao(t.getDescricao());
        dto.setStatus(t.getStatus());
        dto.setPrioridade(t.getPrioridade());
        dto.setDataInicioPlanejada(t.getDataInicioPlanejada());
        dto.setDataFimPlanejada(t.getDataFimPlanejada());
        dto.setDataInicioReal(t.getDataInicioReal());
        dto.setDataFimReal(t.getDataFimReal());
        dto.setPercentualProgresso(t.getPercentualProgresso());
        dto.setEstimativaHoras(t.getEstimativaHoras());
        dto.setSequencia(t.getSequencia());

        java.util.List<Long> predecessorIds = dependenciaRepository.findByTarefaDestinoId(t.getId()).stream()
                .map(DemandaExecucaoTarefaDependencia::getTarefaOrigem)
                .filter(origem -> origem != null)
                .map(origem -> origem.getId())
                .toList();
        dto.setPredecessorIds(predecessorIds);

        java.util.List<DemandaExecucaoGanttRecursoDTO> recursosDto = java.util.stream.Stream.ofNullable(t.getRecursos())
                .flatMap(java.util.List::stream)
                .map(this::toGanttRecursoDTO)
                .toList();
        dto.setRecursos(recursosDto);
        return dto;
    }

    private DemandaExecucaoGanttRecursoDTO toGanttRecursoDTO(DemandaExecucaoTarefaRecurso r) {
        return new DemandaExecucaoGanttRecursoDTO(
                r.getId(),
                r.getProfissional() != null ? r.getProfissional().getId() : null,
                r.getProfissional() != null ? r.getProfissional().getNome() : null,
                r.getPerfil() != null ? r.getPerfil().getId() : null,
                r.getPerfil() != null ? r.getPerfil().getNome() : null,
                r.getHorasPlanejadas(),
                r.getHorasExecutadas()
        );
    }

    @Transactional
    public DemandaExecucaoDTO create(DemandaExecucaoCreateDTO dto) {
        if (repository.findByDemandaId(dto.getDemandaTecnicaId()).isPresent()) {
            throw new BadRequestException("Já existe execução cadastrada para esta demanda técnica.");
        }
        DemandaTecnica demanda = demandaTecnicaRepository.findById(dto.getDemandaTecnicaId())
                .orElseThrow(() -> new ResourceNotFoundException("Demanda técnica não encontrada com ID: " + dto.getDemandaTecnicaId()));

        TermoPlanejamento termoPlanejamento = demanda.getTermoPlanejamento();
        if (termoPlanejamento == null) {
            throw new BadRequestException("Demanda técnica ID " + dto.getDemandaTecnicaId()
                    + " não possui Termo de Planejamento para definir datas de execução.");
        }

        Usuario usuario = dto.getUsuarioId() != null
                ? usuarioRepository.findById(dto.getUsuarioId()).orElse(null)
                : null;

        DemandaExecucao e = new DemandaExecucao();
        e.setDemanda(demanda);
        e.setUsuario(usuario);
        // Datas planejadas vêm do Termo de Planejamento da demanda
        e.setDataInicioPlanejada(termoPlanejamento.getDataInicioExecucao());
        e.setDataFimPlanejada(termoPlanejamento.getDataFimExecucao());
        e.setDataInicioReal(dto.getDataInicioReal());
        e.setDataFimReal(dto.getDataFimReal());
        // Status inicial da execução: PLANEJADA
        e.setStatus("PLANEJADA");
        e.setPercentualProgresso(dto.getPercentualProgresso());
        e.setDataCriacaoExecucao(java.time.LocalDateTime.now());

        return DemandaExecucaoDTO.fromEntity(repository.save(e));
    }

    @Transactional
    public DemandaExecucaoDTO update(Long id, DemandaExecucaoUpdateDTO dto) {
        DemandaExecucao e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Execução da demanda não encontrada com ID: " + id));

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
            e.setUsuario(usuario);
        }
        if (dto.getDataInicioPlanejada() != null) e.setDataInicioPlanejada(dto.getDataInicioPlanejada());
        if (dto.getDataFimPlanejada() != null) e.setDataFimPlanejada(dto.getDataFimPlanejada());
        if (dto.getDataInicioReal() != null) e.setDataInicioReal(dto.getDataInicioReal());
        if (dto.getDataFimReal() != null) e.setDataFimReal(dto.getDataFimReal());
        if (dto.getStatus() != null) e.setStatus(dto.getStatus());
        if (dto.getPercentualProgresso() != null) e.setPercentualProgresso(dto.getPercentualProgresso());

        return DemandaExecucaoDTO.fromEntity(repository.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Execução da demanda não encontrada com ID: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Endpoint de check de horas planejadas por perfil:
     *  A = total de horas (qtdeHora) por perfil em TermoPlanejamentoCusto da demanda;
     *  B = total de horas (horasPlanejadas) por perfil em DemandaExecucaoTarefaRecurso da execução da demanda.
     * Retorna a união dos perfis presentes em A e/ou B com 0 quando ausente em algum lado.
     */
    @Transactional(readOnly = true)
    public List<DemandaExecucaoPerfilCheckDTO> getCheckHorasPorPerfil(Long demandaTecnicaId) {
        if (!demandaTecnicaRepository.existsById(demandaTecnicaId)) {
            throw new ResourceNotFoundException("Demanda técnica não encontrada com ID: " + demandaTecnicaId);
        }

        // Mapas auxiliares por perfilId
        Map<Long, BigDecimal> horasTermoPorPerfil = new HashMap<>();
        Map<Long, BigDecimal> horasExecucaoPorPerfil = new HashMap<>();
        Map<Long, String> nomePorPerfil = new HashMap<>();

        // A) Termo de planejamento -> custos -> agrupar qtdeHora por perfil.id
        termoPlanejamentoRepository.findByDemandaTecnicaId(demandaTecnicaId).ifPresent(termo -> {
            if (termo.getCustos() != null) {
                termo.getCustos().stream()
                        .filter(c -> c.getPerfil() != null)
                        .forEach(c -> {
                            Long perfilId = c.getPerfil().getId();
                            nomePorPerfil.putIfAbsent(perfilId, c.getPerfil().getNome());
                            horasTermoPorPerfil.merge(
                                    perfilId,
                                    safe(c.getQtdeHora()),
                                    BigDecimal::add
                            );
                        });
            }
        });

        // B) Execução da demanda -> recursos -> agrupar horasPlanejadas por perfil.id
        repository.findByDemandaId(demandaTecnicaId).ifPresent(execucao -> {
            List<DemandaExecucaoTarefaRecurso> recursos = recursoRepository
                    .findByDemandaExecucaoTarefa_DemandaExecucaoId(execucao.getId());
            recursos.stream()
                    .filter(r -> r.getPerfil() != null)
                    .forEach(r -> {
                        Long perfilId = r.getPerfil().getId();
                        nomePorPerfil.putIfAbsent(perfilId, r.getPerfil().getNome());
                        horasExecucaoPorPerfil.merge(
                                perfilId,
                                // safe(r.getHorasPlanejadas()),
                                safe(r.getHorasExecutadas()),
                                BigDecimal::add
                        );
                    });
        });

        // Consolidação: união das chaves de A e B
        Set<Long> perfilIds = new HashSet<>();
        perfilIds.addAll(horasTermoPorPerfil.keySet());
        perfilIds.addAll(horasExecucaoPorPerfil.keySet());

        List<DemandaExecucaoPerfilCheckDTO> resultado = new ArrayList<>();
        for (Long perfilId : perfilIds) {
            DemandaExecucaoPerfilCheckDTO dto = new DemandaExecucaoPerfilCheckDTO(
                    perfilId,
                    nomePorPerfil.get(perfilId),
                    horasTermoPorPerfil.getOrDefault(perfilId, BigDecimal.ZERO),
                    horasExecucaoPorPerfil.getOrDefault(perfilId, BigDecimal.ZERO)
            );
            resultado.add(dto);
        }

        // Ordenação estável por nome do perfil (com fallback por id)
        resultado.sort((a, b) -> {
            String na = a.getPerfilNome() != null ? a.getPerfilNome() : "";
            String nb = b.getPerfilNome() != null ? b.getPerfilNome() : "";
            int cmp = na.compareToIgnoreCase(nb);
            if (cmp != 0) return cmp;
            return Long.compare(
                    a.getPerfilId() != null ? a.getPerfilId() : 0L,
                    b.getPerfilId() != null ? b.getPerfilId() : 0L
            );
        });

        return resultado;
    }

    /**
     * Reabre uma execução que foi encerrada (status "CONCLUIDA"), permitindo nova edição
     * pelo usuário. Reverte os efeitos de {@link #encerrarExecucaoInterna(Long, Long)} no que
     * tange a estados de {@link DemandaExecucao} e {@link DemandaTecnica}, sem apagar o
     * {@link com.demandtracker.entity.TermoEncerramento} existente — o encerramento é idempotente
     * e atualizará o termo já existente em uma futura chamada.
     *
     * Regras:
     * - Bloqueia reabertura se a Demanda Técnica estiver com status "G" (Encerrada/assinada),
     *   pois o Termo de Encerramento já foi assinado.
     * - Apenas execuções com status "CONCLUIDA" podem ser reabertas.
     *
     * Após a reabertura:
     * - {@code execucao.status = "EM_ANDAMENTO"};
     * - {@code demanda.status = "E"} (Em execução).
     */
    @Transactional
    public DemandaExecucaoDTO reabrirExecucao(Long demandaExecucaoId) {
        DemandaExecucao execucao = repository.findById(demandaExecucaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Execução da demanda não encontrada com ID: " + demandaExecucaoId));

        DemandaTecnica demanda = execucao.getDemanda();
        if (demanda == null) {
            throw new BadRequestException("Execução ID " + demandaExecucaoId + " não está vinculada a uma Demanda Técnica.");
        }

        // Bloquear reabertura se Termo de Encerramento já estiver assinado (demanda em status G)
        if (DemandaTecnica.STATUS_ENCERRADA.equals(demanda.getStatus())) {
            throw new BadRequestException(
                    "Não é possível reabrir a execução: o Termo de Encerramento já foi assinado (demanda com status 'G' - Encerrada).");
        }

        // Apenas execuções concluídas podem ser reabertas
        if (!"CONCLUIDA".equalsIgnoreCase(execucao.getStatus())) {
            throw new BadRequestException(
                    "Apenas execuções com status CONCLUIDA podem ser reabertas. Status atual: " + execucao.getStatus());
        }

        // Reverte os estados ajustados no encerrarExecucaoInterna
        execucao.setStatus("EM_ANDAMENTO");
        demanda.setStatus(StatusDemandaTecnica.E.getCodigo());

        repository.save(execucao);
        demandaTecnicaRepository.save(demanda);

        return DemandaExecucaoDTO.fromEntity(execucao);
    }

    /**
     * Rotina interna que realiza o encerramento da execução da demanda, cria o Termo de Encerramento
     * e seus custos/profissionais a partir dos recursos planejados nas tarefas.
     *
     * Nome da rotina interna: encerrarExecucaoInterna
     */
    @Transactional
    public TermoEncerramentoDTO encerrarExecucaoInterna(Long demandaExecucaoId, Long usuarioId) {
        DemandaExecucao execucao = repository.findById(demandaExecucaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Execução da demanda não encontrada com ID: " + demandaExecucaoId));

        DemandaTecnica demanda = execucao.getDemanda();
        if (demanda == null) {
            throw new BadRequestException("Execução ID " + demandaExecucaoId + " não está vinculada a uma Demanda Técnica.");
        }

        // 1) Status da execução = CONCLUIDA, 2) percentual = 100
        execucao.setStatus("CONCLUIDA");
        execucao.setPercentualProgresso(java.math.BigDecimal.valueOf(100));

        // 3) Mudar status da demanda técnica para F (Em encerramento)
        demanda.setStatus(StatusDemandaTecnica.F.getCodigo());

        // 4) Iniciar processo de encerramento
        TermoPlanejamento termoPlanejamento = demanda.getTermoPlanejamento();
        if (termoPlanejamento == null) {
            throw new BadRequestException("Demanda técnica ID " + demanda.getId()
                    + " não possui Termo de Planejamento para basear o encerramento.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + usuarioId));

        // Sumarizar horas planejadas de recursos por perfil
        java.util.List<DemandaExecucaoTarefaRecurso> recursos = recursoRepository
                .findByDemandaExecucaoTarefa_DemandaExecucaoId(demandaExecucaoId);

        if (recursos.isEmpty()) {
            throw new BadRequestException("Execução ID " + demandaExecucaoId
                    + " não possui recursos planejados para gerar custos de encerramento.");
        }

        // Agrupar por perfilId (não usar entidade Perfil como chave para evitar StackOverflow em hashCode/equals)
        java.util.Map<Long, java.util.List<DemandaExecucaoTarefaRecurso>> porPerfilId =
                recursos.stream()
                        .filter(r -> r.getPerfil() != null)
                        .collect(java.util.stream.Collectors.groupingBy(r -> r.getPerfil().getId()));

        java.util.List<TermoEncerramentoCustoCreateDTO> custos = new java.util.ArrayList<>();

        for (java.util.Map.Entry<Long, java.util.List<DemandaExecucaoTarefaRecurso>> entry : porPerfilId.entrySet()) {
            java.util.List<DemandaExecucaoTarefaRecurso> recursosDoPerfil = entry.getValue();
            com.demandtracker.entity.Perfil perfil = recursosDoPerfil.get(0).getPerfil();

            java.math.BigDecimal totalHoras = recursosDoPerfil.stream()
                    .map(r -> r.getHorasExecutadas() != null ? r.getHorasExecutadas() : java.math.BigDecimal.ZERO)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            java.util.List<TermoEncerramentoCustoProfissionalItemDTO> itensProfissionais = new java.util.ArrayList<>();
            for (DemandaExecucaoTarefaRecurso r : recursosDoPerfil) {
                TermoEncerramentoCustoProfissionalItemDTO item = new TermoEncerramentoCustoProfissionalItemDTO(
                        r.getProfissional().getId(),
                        r.getHorasExecutadas() != null ? r.getHorasExecutadas() : java.math.BigDecimal.ZERO,
                        perfil.getValor()
                );
                itensProfissionais.add(item);
            }

            TermoEncerramentoCustoCreateDTO custoDTO = new TermoEncerramentoCustoCreateDTO(
                    perfil.getId(),
                    totalHoras,
                    perfil.getValor(),
                    itensProfissionais
            );
            custos.add(custoDTO);
        }

        TermoEncerramentoCreateDTO encerramentoDTO = new TermoEncerramentoCreateDTO();
        encerramentoDTO.setDemandaTecnicaId(demanda.getId());
        encerramentoDTO.setDataTermo(java.time.LocalDateTime.now());
        encerramentoDTO.setDataInicioExecucao(execucao.getDataInicioReal());
        encerramentoDTO.setDataFimExecucao(execucao.getDataFimReal());
        encerramentoDTO.setResultadoEntregue(termoPlanejamento.getResultadoEsperado());
        encerramentoDTO.setUsuarioId(usuario.getId());
        encerramentoDTO.setCustos(custos);

        // Persiste alterações de execução e demanda técnica
        repository.save(execucao);
        demandaTecnicaRepository.save(demanda);

        java.util.Optional<TermoEncerramentoDTO> existente = termoEncerramentoService.findByDemandaIdOptional(demanda.getId());
        if (existente.isPresent()) {
            // Termo já existe: atualizar o termo com os custos e dados (ex.: segundo clique ou termo criado sem custos)
            TermoEncerramentoUpdateDTO updateDTO = new TermoEncerramentoUpdateDTO();
            updateDTO.setDataTermo(encerramentoDTO.getDataTermo());
            updateDTO.setDataInicioExecucao(encerramentoDTO.getDataInicioExecucao());
            updateDTO.setDataFimExecucao(encerramentoDTO.getDataFimExecucao());
            updateDTO.setResultadoEntregue(encerramentoDTO.getResultadoEntregue());
            updateDTO.setCustos(custos);
            return termoEncerramentoService.update(existente.get().getId(), updateDTO);
        }

        // Termo não existe: criar novo Termo de Encerramento
        return termoEncerramentoService.create(encerramentoDTO);
    }

    private LocalDate resolveInicioExecucao(DemandaExecucaoTarefaRecurso recurso) {
        if (recurso == null || recurso.getDemandaExecucaoTarefa() == null
                || recurso.getDemandaExecucaoTarefa().getDemandaExecucao() == null) {
            return null;
        }
        DemandaExecucao execucao = recurso.getDemandaExecucaoTarefa().getDemandaExecucao();
        return execucao.getDataInicioReal() != null ? execucao.getDataInicioReal() : execucao.getDataInicioPlanejada();
    }

    private LocalDate resolveFimExecucao(DemandaExecucaoTarefaRecurso recurso) {
        if (recurso == null || recurso.getDemandaExecucaoTarefa() == null
                || recurso.getDemandaExecucaoTarefa().getDemandaExecucao() == null) {
            return null;
        }
        DemandaExecucao execucao = recurso.getDemandaExecucaoTarefa().getDemandaExecucao();
        return execucao.getDataFimReal() != null ? execucao.getDataFimReal() : execucao.getDataFimPlanejada();
    }

    private Map<YearMonth, BigDecimal> ratearHorasPorDiasUteis(BigDecimal totalHoras, LocalDate inicio, LocalDate fim) {
        Map<YearMonth, BigDecimal> result = new HashMap<>();
        int totalDiasUteis = contarDiasUteisNoIntervalo(inicio, fim);
        if (totalDiasUteis <= 0 || totalHoras == null || totalHoras.compareTo(BigDecimal.ZERO) <= 0) {
            return result;
        }

        YearMonth ym = YearMonth.from(inicio);
        YearMonth fimYm = YearMonth.from(fim);
        List<YearMonth> meses = new ArrayList<>();
        while (!ym.isAfter(fimYm)) {
            meses.add(ym);
            ym = ym.plusMonths(1);
        }

        BigDecimal acumulado = BigDecimal.ZERO;
        for (int i = 0; i < meses.size(); i++) {
            YearMonth m = meses.get(i);
            LocalDate iniMes = m.equals(YearMonth.from(inicio)) ? inicio : m.atDay(1);
            LocalDate fimMes = m.equals(YearMonth.from(fim)) ? fim : m.atEndOfMonth();
            int diasMes = contarDiasUteisNoIntervalo(iniMes, fimMes);

            BigDecimal horasMes;
            if (i == meses.size() - 1) {
                horasMes = totalHoras.subtract(acumulado);
            } else {
                horasMes = totalHoras
                        .multiply(BigDecimal.valueOf(diasMes))
                        .divide(BigDecimal.valueOf(totalDiasUteis), 2, RoundingMode.HALF_UP);
                acumulado = acumulado.add(horasMes);
            }
            result.put(m, safe(horasMes));
        }
        return result;
    }

    private int contarDiasUteisNoIntervalo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            return 0;
        }
        Set<LocalDate> feriados = feriadosNacionaisNoIntervalo(inicio.getYear(), fim.getYear());
        int dias = 0;
        LocalDate d = inicio;
        while (!d.isAfter(fim)) {
            DayOfWeek dow = d.getDayOfWeek();
            boolean fimSemana = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
            // if (!fimSemana && !feriados.contains(d)) {
            if (!fimSemana ) {
                dias++;
            }
            d = d.plusDays(1);
        }
        return dias;
    }

    private Set<LocalDate> feriadosNacionaisNoIntervalo(int anoInicio, int anoFim) {
        Set<LocalDate> out = new HashSet<>();
        for (int ano = anoInicio; ano <= anoFim; ano++) {
            // Feriados fixos nacionais
            out.add(LocalDate.of(ano, 1, 1));
            out.add(LocalDate.of(ano, 4, 21));
            out.add(LocalDate.of(ano, 5, 1));
            out.add(LocalDate.of(ano, 9, 7));
            out.add(LocalDate.of(ano, 10, 12));
            out.add(LocalDate.of(ano, 11, 2));
            out.add(LocalDate.of(ano, 11, 15));
            out.add(LocalDate.of(ano, 12, 25));

            // Feriados móveis nacionais baseados na Páscoa
            LocalDate pascoa = calcularPascoa(ano);
            out.add(pascoa.minusDays(48)); // Carnaval (segunda)
            out.add(pascoa.minusDays(47)); // Carnaval (terca)
            out.add(pascoa.minusDays(2));  // Sexta-feira Santa
            out.add(pascoa.plusDays(60));  // Corpus Christi
        }
        return out;
    }

    private LocalDate calcularPascoa(int ano) {
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(ano, mes, dia);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
