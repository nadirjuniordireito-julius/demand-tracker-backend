package com.demandtracker.dto;

import com.demandtracker.entity.UsuarioProjeto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioProjetoDTO {
    private Long id;
    private Long usuarioId;
    private UsuarioDTO usuario;
    private Long projetoId;
    private ProjetoDTO projeto;
    
    public static UsuarioProjetoDTO fromEntity(UsuarioProjeto usuarioProjeto) {
        UsuarioProjetoDTO dto = new UsuarioProjetoDTO();
        dto.setId(usuarioProjeto.getId());
        dto.setUsuarioId(usuarioProjeto.getUsuario().getId());
        if (usuarioProjeto.getUsuario() != null) {
            dto.setUsuario(UsuarioDTO.fromEntity(usuarioProjeto.getUsuario()));
        }
        dto.setProjetoId(usuarioProjeto.getProjeto().getId());
        if (usuarioProjeto.getProjeto() != null) {
            dto.setProjeto(ProjetoDTO.fromEntity(usuarioProjeto.getProjeto()));
        }
        return dto;
    }
}
