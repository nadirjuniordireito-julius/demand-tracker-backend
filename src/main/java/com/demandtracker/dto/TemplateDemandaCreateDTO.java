package com.demandtracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para criação de TemplateDemanda
 */
@Data
public class TemplateDemandaCreateDTO {
    
    @NotNull(message = "ID do Projeto é obrigatório")
    private Long projetoId;
    
    @NotNull(message = "Tipo é obrigatório")
    private String tipo;
    
    @NotNull(message = "Arquivo DOCX é obrigatório")
    private MultipartFile arquivoDocx;
}
