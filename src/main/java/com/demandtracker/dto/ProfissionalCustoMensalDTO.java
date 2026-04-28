package com.demandtracker.dto;

import com.demandtracker.entity.ProfissionalCustoMensal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalCustoMensalDTO {

    private Long id;
    private Long profissionalId;
    private ProfissionalDTO profissional;
    private Integer ano;
    private Integer mes;
    private BigDecimal custoTotal;

    public static ProfissionalCustoMensalDTO fromEntity(ProfissionalCustoMensal entity) {
        if (entity == null) {
            return null;
        }
        ProfissionalCustoMensalDTO dto = new ProfissionalCustoMensalDTO();
        dto.setId(entity.getId());
        dto.setAno(entity.getAno());
        dto.setMes(entity.getMes());
        dto.setCustoTotal(entity.getCustoTotal());
        if (entity.getProfissional() != null) {
            dto.setProfissionalId(entity.getProfissional().getId());
            dto.setProfissional(ProfissionalDTO.fromEntity(entity.getProfissional()));
        }
        return dto;
    }
}
