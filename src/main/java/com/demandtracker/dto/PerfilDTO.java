package com.demandtracker.dto;

import com.demandtracker.entity.Perfil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilDTO {
    private Long id;
    private String nome;
    private LocalDate termoInicial;
    private LocalDate termoFinal;
    private LocalDateTime dataUpdate;
    private BigDecimal valor;
    private Long usuarioId;
    private UsuarioDTO usuario;
    private Long projetoId;
    private ProjetoDTO projeto;
    
    public static PerfilDTO fromEntity(Perfil perfil) {
        PerfilDTO dto = new PerfilDTO();
        dto.setId(perfil.getId());
        dto.setNome(perfil.getNome());
        dto.setTermoInicial(perfil.getTermoInicial());
        dto.setTermoFinal(perfil.getTermoFinal());
        dto.setDataUpdate(perfil.getDataUpdate());
        dto.setValor(perfil.getValor());
        if (perfil.getUsuario() != null) {
            dto.setUsuarioId(perfil.getUsuario().getId());
            dto.setUsuario(UsuarioDTO.fromEntity(perfil.getUsuario()));
        }
        if (perfil.getProjeto() != null) {
            dto.setProjetoId(perfil.getProjeto().getId());
            dto.setProjeto(ProjetoDTO.fromEntity(perfil.getProjeto()));
        }
        return dto;
    }
}
