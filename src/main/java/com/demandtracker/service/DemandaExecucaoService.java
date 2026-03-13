package com.demandtracker.service;

import com.demandtracker.dto.DemandaExecucaoCreateDTO;
import com.demandtracker.dto.DemandaExecucaoDTO;
import com.demandtracker.dto.DemandaExecucaoGanttDTO;
import com.demandtracker.dto.DemandaExecucaoGanttRecursoDTO;
import com.demandtracker.dto.DemandaExecucaoGanttTarefaDTO;
import com.demandtracker.dto.DemandaExecucaoUpdateDTO;
import com.demandtracker.dto.TermoEncerramentoCreateDTO;
import com.demandtracker.dto.TermoEncerramentoCustoCreateDTO;
import com.demandtracker.dto.TermoEncerramentoCustoProfissionalItemDTO;
import com.demandtracker.dto.TermoEncerramentoDTO;
import com.demandtracker.dto.TermoEncerramentoUpdateDTO;
import com.demandtracker.entity.DemandaExecucao;
import com.demandtracker.entity.DemandaExecucaoTarefa;
import com.demandtracker.entity.DemandaExecucaoTarefaDependencia;
import com.demandtracker.entity.DemandaExecucaoTarefaRecurso;
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
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemandaExecucaoService {

    private final DemandaExecucaoRepository repository;
    private final DemandaTecnicaRepository demandaTecnicaRepository;
    private final DemandaExecucaoTarefaDependenciaRepository dependenciaRepository;
    private final DemandaExecucaoTarefaRecursoRepository recursoRepository;
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
                r.getHorasPlanejadas()
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
                        .filter(r -> r.getProfissional() != null && r.getProfissional().getPerfil() != null)
                        .collect(java.util.stream.Collectors.groupingBy(r -> r.getProfissional().getPerfil().getId()));

        java.util.List<TermoEncerramentoCustoCreateDTO> custos = new java.util.ArrayList<>();

        for (java.util.Map.Entry<Long, java.util.List<DemandaExecucaoTarefaRecurso>> entry : porPerfilId.entrySet()) {
            java.util.List<DemandaExecucaoTarefaRecurso> recursosDoPerfil = entry.getValue();
            com.demandtracker.entity.Perfil perfil = recursosDoPerfil.get(0).getProfissional().getPerfil();

            java.math.BigDecimal totalHoras = recursosDoPerfil.stream()
                    .map(DemandaExecucaoTarefaRecurso::getHorasPlanejadas)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            java.util.List<TermoEncerramentoCustoProfissionalItemDTO> itensProfissionais = new java.util.ArrayList<>();
            for (DemandaExecucaoTarefaRecurso r : recursosDoPerfil) {
                TermoEncerramentoCustoProfissionalItemDTO item = new TermoEncerramentoCustoProfissionalItemDTO(
                        r.getProfissional().getId(),
                        r.getHorasPlanejadas(),
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
}
