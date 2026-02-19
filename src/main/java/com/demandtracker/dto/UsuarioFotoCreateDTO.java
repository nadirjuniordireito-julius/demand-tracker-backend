package com.demandtracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para criação de UsuarioFoto
 */
@Data
public class UsuarioFotoCreateDTO {
    
    @NotNull(message = "ID do usuário é obrigatório")
    private Long usuarioId;
    
    @NotNull(message = "Foto é obrigatória")
    private MultipartFile foto;
}
