package com.demandtracker.service;

import com.demandtracker.dto.ProfissionalAnaliseResumidaDTO;
import com.demandtracker.dto.ProfissionalCreateDTO;
import com.demandtracker.dto.ProfissionalDTO;
import com.demandtracker.dto.ProfissionalUpdateDTO;
import com.demandtracker.entity.DemandaExecucaoTarefa;
import com.demandtracker.entity.DemandaExecucaoTarefaRecurso;
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
