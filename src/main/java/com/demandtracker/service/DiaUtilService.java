package com.demandtracker.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DiaUtilService {

    public static final int HORAS_POR_DIA_UTIL = 8;

    public int contarDiasUteis(LocalDate inicio, LocalDate fim, Set<LocalDate> diasNaoUtil) {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            return 0;
        }
        int dias = 0;
        LocalDate d = inicio;
        while (!d.isAfter(fim)) {
            if (isDiaUtil(d, diasNaoUtil)) {
                dias++;
            }
            d = d.plusDays(1);
        }
        return dias;
    }

    /**
     * Calcula horas por mês: dias úteis no recorte da tarefa × {@link #HORAS_POR_DIA_UTIL}.
     * Sem divisão proporcional do total. A soma dos meses fica entre o bruto calculado e
     * {@code horasExecutadasLimite}: se o bruto for menor, o saldo vai ao último mês com horas;
     * se for maior, aplica teto sequencial por mês.
     */
    public Map<YearMonth, BigDecimal> calcularHorasPorMesPorDiasUteis(
            BigDecimal horasExecutadasLimite,
            LocalDate inicio,
            LocalDate fim,
            Set<LocalDate> diasNaoUtil) {
        Map<YearMonth, BigDecimal> result = new HashMap<>();
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            return result;
        }

        YearMonth ymInicio = YearMonth.from(inicio);
        YearMonth ymFim = YearMonth.from(fim);
        List<YearMonth> meses = new ArrayList<>();
        YearMonth cursor = ymInicio;
        while (!cursor.isAfter(ymFim)) {
            meses.add(cursor);
            cursor = cursor.plusMonths(1);
        }

        Map<YearMonth, BigDecimal> horasBrutasPorMes = new HashMap<>();
        for (YearMonth m : meses) {
            LocalDate iniMes = m.equals(ymInicio) ? inicio : m.atDay(1);
            LocalDate fimMes = m.equals(ymFim) ? fim : m.atEndOfMonth();
            int diasUteis = contarDiasUteis(iniMes, fimMes, diasNaoUtil);
            BigDecimal horasBrutas = BigDecimal.valueOf(diasUteis)
                    .multiply(BigDecimal.valueOf(HORAS_POR_DIA_UTIL))
                    .setScale(2, RoundingMode.HALF_UP);
            if (horasBrutas.compareTo(BigDecimal.ZERO) > 0) {
                horasBrutasPorMes.put(m, horasBrutas);
            }
        }

        BigDecimal limite = horasExecutadasLimite != null ? horasExecutadasLimite : BigDecimal.ZERO;
        BigDecimal somaBruta = horasBrutasPorMes.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (limite.compareTo(BigDecimal.ZERO) <= 0) {
            return horasBrutasPorMes;
        }
        if (somaBruta.compareTo(limite) < 0) {
            BigDecimal saldo = limite.subtract(somaBruta).setScale(2, RoundingMode.HALF_UP);
            YearMonth mesDestino = ultimoMesComHorasBrutas(meses, horasBrutasPorMes);
            if (mesDestino != null) {
                horasBrutasPorMes.merge(mesDestino, saldo, BigDecimal::add);
            }
            return horasBrutasPorMes;
        }
        if (somaBruta.compareTo(limite) == 0) {
            return horasBrutasPorMes;
        }

        BigDecimal saldo = limite.setScale(2, RoundingMode.HALF_UP);
        for (YearMonth m : meses) {
            BigDecimal bruta = horasBrutasPorMes.getOrDefault(m, BigDecimal.ZERO);
            if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal horasMes = bruta.min(saldo).setScale(2, RoundingMode.HALF_UP);
            if (horasMes.compareTo(BigDecimal.ZERO) > 0) {
                result.put(m, horasMes);
            }
            saldo = saldo.subtract(horasMes);
        }
        return result;
    }

    /**
     * @deprecated Use {@link #calcularHorasPorMesPorDiasUteis(BigDecimal, LocalDate, LocalDate, Set)}.
     */
    @Deprecated
    public Map<YearMonth, BigDecimal> ratearHorasPorDiasUteis(
            BigDecimal totalHoras,
            LocalDate inicio,
            LocalDate fim,
            Set<LocalDate> diasNaoUtil) {
        return calcularHorasPorMesPorDiasUteis(totalHoras, inicio, fim, diasNaoUtil);
    }

    private static YearMonth ultimoMesComHorasBrutas(List<YearMonth> meses, Map<YearMonth, BigDecimal> horasBrutasPorMes) {
        YearMonth ultimo = null;
        for (YearMonth m : meses) {
            if (horasBrutasPorMes.getOrDefault(m, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
                ultimo = m;
            }
        }
        return ultimo;
    }

    private boolean isDiaUtil(LocalDate data, Set<LocalDate> diasNaoUtil) {
        DayOfWeek dow = data.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return false;
        }
        return diasNaoUtil == null || !diasNaoUtil.contains(data);
    }
}
