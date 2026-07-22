package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalDemandasTecnicasResponseDTO {

    private List<ProfissionalDemandaTecnicaDTO> demandasTecnicas = new ArrayList<>();
    private List<ProfissionalDemandaTecnicaResumoMensalDTO> resumoMensal = new ArrayList<>();
}
