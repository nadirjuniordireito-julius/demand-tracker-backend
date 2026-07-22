package com.demandtracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalDemandaTecnicaDTO {

    private Long demandaTecnicaId;
    private String demandaCodigo;
    private String demandaNome;
    private String demandaStatus;
    private BigDecimal totalHorasExecutadas;
    private BigDecimal totalHorasPlanejadas;
    private BigDecimal totalHorasUteisPeriodo;
    private List<ProfissionalDemandaTecnicaMensalDTO> totaisMensais = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicioExecucao;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFimExecucao;
}
