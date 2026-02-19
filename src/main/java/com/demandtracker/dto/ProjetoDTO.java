package com.demandtracker.dto;

import com.demandtracker.entity.Projeto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoDTO {
    private Long id;
    private String nome;
    private String codTed;
    private LocalDate termoInicial;
    private LocalDate termoFinal;
    private LocalDate dataEfetivaInicio;
    private LocalDateTime dataUpdate;
    private Long usuarioId;
    private UsuarioDTO usuario;
    
    public static ProjetoDTO fromEntity(Projeto projeto) {
        ProjetoDTO dto = new ProjetoDTO();
        dto.setId(projeto.getId());
        dto.setNome(projeto.getNome());
        dto.setCodTed(projeto.getCodTed());
        dto.setTermoInicial(projeto.getTermoInicial());
        dto.setTermoFinal(projeto.getTermoFinal());
        dto.setDataEfetivaInicio(projeto.getDataEfetivaInicio());
        dto.setDataUpdate(projeto.getDataUpdate());
        dto.setUsuarioId(projeto.getUsuario().getId());
        if (projeto.getUsuario() != null) {
            dto.setUsuario(UsuarioDTO.fromEntity(projeto.getUsuario()));
        }
        return dto;
    }
}
