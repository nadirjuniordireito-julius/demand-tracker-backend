package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO reduzido de usuário para exibição na timeline da demanda técnica.
 * fotoUrl é o caminho para obter a imagem; nulo se o usuário não possui foto em UsuarioFoto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioTimelineDTO {
    private Long id;
    private String nome;
    private String email;
    /**
     * URL/caminho para download da foto: GET /api/usuario-foto/usuario/{id}/download.
     * Nulo se não existir registro em UsuarioFoto para este usuário.
     */
    private String fotoUrl;
}
