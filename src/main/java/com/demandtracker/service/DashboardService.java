package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final DemandaTecnicaRepository demandaRepository;
    private final TermoPlanejamentoRepository termoPlanejamentoRepository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;
    private final ProjetoRepository projetoRepository;
    
    public DashboardStatsDTO getStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalDemandas(demandaRepository.countTotal());
        stats.setDemandasAbertas(demandaRepository.countAbertas());
        stats.setDemandasEncerradas(demandaRepository.countEncerradas());
        
        BigDecimal custosPlanejados = termoPlanejamentoRepository.sumCustosPlanejados();
        stats.setCustosPlanejados(custosPlanejados != null ? custosPlanejados : BigDecimal.ZERO);
        
        BigDecimal custosRealizados = termoEncerramentoRepository.sumCustosRealizados();
        stats.setCustosRealizados(custosRealizados != null ? custosRealizados : BigDecimal.ZERO);
        
        return stats;
    }
    
    public List<DemandaPorProjetoDTO> getDemandasPorProjeto() {
        List<DemandaPorProjetoDTO> result = new ArrayList<>();
        projetoRepository.findAll().forEach(projeto -> {
            long count = demandaRepository.findByProjetoId(projeto.getId(), org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
            if (count > 0) {
                result.add(new DemandaPorProjetoDTO(projeto.getNome(), count));
            }
        });
        return result;
    }
    
    public List<DemandaPorStatusDTO> getDemandasPorStatus() {
        List<DemandaPorStatusDTO> result = new ArrayList<>();
        
        long abertas = demandaRepository.countAbertas();
        long encerradas = demandaRepository.countEncerradas();
        
        long emPlanejamento = demandaRepository.findAll().stream()
            .filter(d -> d.getTermoAbertura() != null && d.getTermoAbertura().getDataAssinatura() != null 
                && d.getTermoPlanejamento() == null)
            .count();
        
        long emExecucao = demandaRepository.findAll().stream()
            .filter(d -> d.getTermoPlanejamento() != null && d.getTermoPlanejamento().getDataAssinatura() != null 
                && d.getTermoEncerramento() == null)
            .count();
        
        result.add(new DemandaPorStatusDTO("Abertas", abertas, "#8884d8"));
        result.add(new DemandaPorStatusDTO("Em Planejamento", emPlanejamento, "#82ca9d"));
        result.add(new DemandaPorStatusDTO("Em Execução", emExecucao, "#ffc658"));
        result.add(new DemandaPorStatusDTO("Encerradas", encerradas, "#ff7300"));
        
        return result;
    }
}
