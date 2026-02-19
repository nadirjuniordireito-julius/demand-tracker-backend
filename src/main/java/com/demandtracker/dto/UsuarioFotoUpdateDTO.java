package com.demandtracker.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para atualização de UsuarioFoto
 */
@Data
public class UsuarioFotoUpdateDTO {
    
    private MultipartFile foto;
}
