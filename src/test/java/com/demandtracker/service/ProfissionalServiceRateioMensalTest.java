package com.demandtracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.demandtracker.dto.ProfissionalDemandaTecnicaDTO;
import com.demandtracker.dto.ProfissionalDemandaTecnicaMensalDTO;
import com.demandtracker.dto.ProfissionalDemandaTecnicaResumoMensalDTO;
import com.demandtracker.entity.ProfissionalCustoMensal;
import com.demandtracker.repository.DemandaExecucaoRepository;
import com.demandtracker.repository.DemandaExecucaoTarefaRecursoRepository;
import com.demandtracker.repository.DiaNaoUtilRepository;
import com.demandtracker.repository.PerfilRepository;
import com.demandtracker.repository.ProfissionalCustoMensalRepository;
import com.demandtracker.repository.ProfissionalRepository;
import com.demandtracker.repository.ProjetoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceRateioMensalTest {

    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private ProjetoRepository projetoRepository;
    @Mock private PerfilRepository perfilRepository;
    @Mock private DemandaExecucaoRepository demandaExecucaoRepository;
    @Mock private DemandaExecucaoTarefaRecursoRepository demandaExecucaoTarefaRecursoRepository;
    @Mock private ProfissionalCustoMensalRepository profissionalCustoMensalRepository;
    @Mock private DiaNaoUtilRepository diaNaoUtilRepository;

    private ProfissionalService service;

    @BeforeEach
    void setUp() {
        service = new ProfissionalService(
                profissionalRepository,
                projetoRepository,
                perfilRepository,
                demandaExecucaoRepository,
                demandaExecucaoTarefaRecursoRepository,
                profissionalCustoMensalRepository,
                diaNaoUtilRepository,
                new DiaUtilService()
        );
    }

    @Test
    void mesUnico_capacidade120_lancado104_vale104() {
        // 11/05/2026 a 31/05/2026: 15 úteis × 8 = 120
        Map<YearMonth, BigDecimal> horas = service.alocarHorasPorMesComTeto(
                new BigDecimal("104.00"),
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 31),
                Set.of());

        assertThat(horas.get(YearMonth.of(2026, 5))).isEqualByComparingTo("104.00");
        assertThat(soma(horas)).isEqualByComparingTo("104.00");
    }

    @Test
    void cruzandoMeses_lançado80_jun56_jul24() {
        // 22/06/2026 a 10/07/2026: jun 7×8=56; jul absorve restante 24
        Map<YearMonth, BigDecimal> horas = service.alocarHorasPorMesComTeto(
                new BigDecimal("80.00"),
                LocalDate.of(2026, 6, 22),
                LocalDate.of(2026, 7, 10),
                Set.of());

        assertThat(horas.get(YearMonth.of(2026, 6))).isEqualByComparingTo("56.00");
        assertThat(horas.get(YearMonth.of(2026, 7))).isEqualByComparingTo("24.00");
        assertThat(soma(horas)).isEqualByComparingTo("80.00");
    }

    @Test
    void planejadoEExecutado_totaisDiferentes_mapasDistintos() {
        LocalDate inicio = LocalDate.of(2026, 6, 22);
        LocalDate fim = LocalDate.of(2026, 7, 10);

        Map<YearMonth, BigDecimal> planejado = service.alocarHorasPorMesComTeto(
                new BigDecimal("80.00"), inicio, fim, Set.of());
        Map<YearMonth, BigDecimal> executado = service.alocarHorasPorMesComTeto(
                new BigDecimal("64.00"), inicio, fim, Set.of());

        assertThat(planejado.get(YearMonth.of(2026, 6))).isEqualByComparingTo("56.00");
        assertThat(planejado.get(YearMonth.of(2026, 7))).isEqualByComparingTo("24.00");
        assertThat(executado.get(YearMonth.of(2026, 6))).isEqualByComparingTo("56.00");
        assertThat(executado.get(YearMonth.of(2026, 7))).isEqualByComparingTo("8.00");
        assertThat(soma(planejado)).isEqualByComparingTo("80.00");
        assertThat(soma(executado)).isEqualByComparingTo("64.00");
    }

    @Test
    void horasZero_mapaVazio() {
        Map<YearMonth, BigDecimal> horas = service.alocarHorasPorMesComTeto(
                BigDecimal.ZERO,
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 31),
                Set.of());

        assertThat(horas).isEmpty();
    }

    @Test
    void feriadoReduzCapacidadeEAlteraTeto() {
        LocalDate inicio = LocalDate.of(2026, 6, 22);
        LocalDate fim = LocalDate.of(2026, 7, 10);
        Set<LocalDate> feriado = Set.of(LocalDate.of(2026, 6, 24)); // qua no período de junho

        Map<YearMonth, BigDecimal> semFeriado = service.alocarHorasPorMesComTeto(
                new BigDecimal("80.00"), inicio, fim, Set.of());
        Map<YearMonth, BigDecimal> comFeriado = service.alocarHorasPorMesComTeto(
                new BigDecimal("80.00"), inicio, fim, feriado);

        // junho: capacidade 56 → 48; ainda assim jun recebe min(48, 80)=48; jul recebe 32
        assertThat(comFeriado.get(YearMonth.of(2026, 6))).isEqualByComparingTo("48.00");
        assertThat(comFeriado.get(YearMonth.of(2026, 7))).isEqualByComparingTo("32.00");
        assertThat(comFeriado.get(YearMonth.of(2026, 6)))
                .isLessThan(semFeriado.get(YearMonth.of(2026, 6)));
        assertThat(soma(comFeriado)).isEqualByComparingTo("80.00");
    }

    @Test
    void contarDiasUteisPorMes_respeitaRecortes() {
        Map<YearMonth, Integer> dias = service.contarDiasUteisPorMes(
                LocalDate.of(2026, 6, 22),
                LocalDate.of(2026, 7, 10),
                Set.of());

        assertThat(dias.get(YearMonth.of(2026, 6))).isEqualTo(7);
        assertThat(dias.get(YearMonth.of(2026, 7))).isEqualTo(8); // 01–10/07 sem feriado
    }

    @Test
    void montarResumoMensal_agregaDtsEAplicaCustos() {
        ProfissionalDemandaTecnicaDTO dt1 = new ProfissionalDemandaTecnicaDTO();
        dt1.setTotaisMensais(List.of(
                new ProfissionalDemandaTecnicaMensalDTO(2026, 6, bd("56.00"), bd("56.00")),
                new ProfissionalDemandaTecnicaMensalDTO(2026, 7, bd("24.00"), bd("24.00"))
        ));
        ProfissionalDemandaTecnicaDTO dt2 = new ProfissionalDemandaTecnicaDTO();
        dt2.setTotaisMensais(List.of(
                new ProfissionalDemandaTecnicaMensalDTO(2026, 6, bd("40.00"), bd("32.00"))
        ));

        ProfissionalCustoMensal custoJun = new ProfissionalCustoMensal();
        custoJun.setId(1L);
        custoJun.setAno(2026);
        custoJun.setMes(6);
        custoJun.setCustoTotal(bd("5000.00"));

        Map<YearMonth, ProfissionalCustoMensal> custos = new HashMap<>();
        custos.put(YearMonth.of(2026, 6), custoJun);

        // junho/2026: 22 úteis × 8 = 176; julho/2026: 23 úteis × 8 = 184
        List<ProfissionalDemandaTecnicaResumoMensalDTO> resumo = service.montarResumoMensal(
                List.of(dt1, dt2), bd("100.00"), custos, Set.of(), null);

        assertThat(resumo).hasSize(2);

        ProfissionalDemandaTecnicaResumoMensalDTO jun = resumo.get(0);
        assertThat(jun.getAno()).isEqualTo(2026);
        assertThat(jun.getMes()).isEqualTo(6);
        assertThat(jun.getTotalPlanejado()).isEqualByComparingTo("96.00");
        assertThat(jun.getTotalExecutado()).isEqualByComparingTo("88.00");
        assertThat(jun.getValorCustoPerfil()).isEqualByComparingTo("8800.00");
        assertThat(jun.getValorCustoMensal()).isEqualByComparingTo("5000.00");
        assertThat(jun.getHorasPrevistas()).isEqualByComparingTo("176.00");

        ProfissionalDemandaTecnicaResumoMensalDTO jul = resumo.get(1);
        assertThat(jul.getMes()).isEqualTo(7);
        assertThat(jul.getTotalPlanejado()).isEqualByComparingTo("24.00");
        assertThat(jul.getTotalExecutado()).isEqualByComparingTo("24.00");
        assertThat(jul.getValorCustoPerfil()).isEqualByComparingTo("2400.00");
        assertThat(jul.getValorCustoMensal()).isEqualByComparingTo("0.00");
        assertThat(jul.getHorasPrevistas()).isEqualByComparingTo("184.00");
    }

    @Test
    void montarResumoMensal_feriadoReduzHorasPrevistas() {
        ProfissionalDemandaTecnicaDTO dt = new ProfissionalDemandaTecnicaDTO();
        dt.setTotaisMensais(List.of(
                new ProfissionalDemandaTecnicaMensalDTO(2026, 6, bd("56.00"), bd("56.00"))
        ));

        Set<LocalDate> feriado = Set.of(LocalDate.of(2026, 6, 4)); // qui — Corpus Christi

        List<ProfissionalDemandaTecnicaResumoMensalDTO> semFeriado = service.montarResumoMensal(
                List.of(dt), bd("100.00"), Map.of(), Set.of(), null);
        List<ProfissionalDemandaTecnicaResumoMensalDTO> comFeriado = service.montarResumoMensal(
                List.of(dt), bd("100.00"), Map.of(), feriado, null);

        // junho/2026: 22 → 21 úteis × 8 = 168
        assertThat(semFeriado.get(0).getHorasPrevistas()).isEqualByComparingTo("176.00");
        assertThat(comFeriado.get(0).getHorasPrevistas()).isEqualByComparingTo("168.00");
        assertThat(comFeriado.get(0).getHorasPrevistas())
                .isLessThan(semFeriado.get(0).getHorasPrevistas());
    }

    @Test
    void montarResumoMensal_admissaoNoMeioDoMes_reduzHorasPrevistas() {
        ProfissionalDemandaTecnicaDTO dt = new ProfissionalDemandaTecnicaDTO();
        dt.setTotaisMensais(List.of(
                new ProfissionalDemandaTecnicaMensalDTO(2026, 3, bd("40.00"), bd("40.00")),
                new ProfissionalDemandaTecnicaMensalDTO(2026, 4, bd("40.00"), bd("40.00"))
        ));

        LocalDate admissao = LocalDate.of(2026, 3, 16);
        List<ProfissionalDemandaTecnicaResumoMensalDTO> semAdmissao = service.montarResumoMensal(
                List.of(dt), bd("100.00"), Map.of(), Set.of(), null);
        List<ProfissionalDemandaTecnicaResumoMensalDTO> comAdmissao = service.montarResumoMensal(
                List.of(dt), bd("100.00"), Map.of(), Set.of(), admissao);

        // março: 176 → 96 (16/03–31/03); abril permanece mês cheio
        assertThat(semAdmissao.get(0).getHorasPrevistas()).isEqualByComparingTo("176.00");
        assertThat(comAdmissao.get(0).getHorasPrevistas()).isEqualByComparingTo("96.00");
        assertThat(comAdmissao.get(0).getHorasPrevistas())
                .isLessThan(semAdmissao.get(0).getHorasPrevistas());
        assertThat(comAdmissao.get(1).getHorasPrevistas())
                .isEqualByComparingTo(semAdmissao.get(1).getHorasPrevistas());
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private static BigDecimal soma(Map<YearMonth, BigDecimal> horas) {
        return horas.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
