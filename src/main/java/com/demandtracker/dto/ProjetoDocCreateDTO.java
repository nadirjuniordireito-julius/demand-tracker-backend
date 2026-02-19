package com.demandtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para criação de ProjetoDoc
 */
@Data
public class ProjetoDocCreateDTO {
    
    @NotNull(message = "ID do projeto é obrigatório")
    private Long projetoId;
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @NotNull(message = "Documento é obrigatório")
    private MultipartFile documento;
}
