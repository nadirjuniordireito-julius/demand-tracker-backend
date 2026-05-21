package com.demandtracker.dto;

import com.demandtracker.entity.DiaNaoUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaNaoUtilDTO {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    private String descricao;

    public static DiaNaoUtilDTO fromEntity(DiaNaoUtil entity) {
        if (entity == null) {
            return null;
        }
        DiaNaoUtilDTO dto = new DiaNaoUtilDTO();
        dto.setId(entity.getId());
        dto.setData(entity.getData());
        dto.setDescricao(entity.getDescricao());
        return dto;
    }
}
