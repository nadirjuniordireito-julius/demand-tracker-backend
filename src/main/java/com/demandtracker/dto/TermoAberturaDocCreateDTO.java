package com.demandtracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * DTO para criação de TermoAberturaDoc
 */
@Data
public class TermoAberturaDocCreateDTO {
    
    @NotNull(message = "ID do Termo de Abertura é obrigatório")
    private Long termoAberturaId;
    
    private LocalDateTime dataAssinatura;
    
    @NotNull(message = "Arquivo PDF é obrigatório")
    private MultipartFile arquivoPdf;
}
