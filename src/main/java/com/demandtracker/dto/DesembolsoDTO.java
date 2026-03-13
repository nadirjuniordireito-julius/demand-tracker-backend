package com.demandtracker.dto;

import com.demandtracker.entity.Desembolso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesembolsoDTO {

    private Long id;
    private String documento;
    private BigDecimal valorPrevisto;
    private BigDecimal valor;
    private LocalDate dataDesembolso;
    private LocalDate dataPrevistaDesembolso;
    private Long projetoId;
    private ProjetoDTO projeto;

    public static DesembolsoDTO fromEntity(Desembolso desembolso) {
        DesembolsoDTO dto = new DesembolsoDTO();
        dto.setId(desembolso.getId());
        dto.setDocumento(desembolso.getDocumento());
        dto.setValorPrevisto(desembolso.getValorPrevisto());
        dto.setValor(desembolso.getValor());
        dto.setDataDesembolso(desembolso.getDataDesembolso());
        dto.setDataPrevistaDesembolso(desembolso.getDataPrevistaDesembolso());
        if (desembolso.getProjeto() != null) {
            dto.setProjetoId(desembolso.getProjeto().getId());
            dto.setProjeto(ProjetoDTO.fromEntity(desembolso.getProjeto()));
        }
        return dto;
    }
}

