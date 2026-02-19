package com.demandtracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * DTO para criação de TermoPlanejamentoDoc
 */
@Data
public class TermoPlanejamentoDocCreateDTO {
    
    @NotNull(message = "ID do Termo de Planejamento é obrigatório")
    private Long termoPlanejamentoId;
    
    private LocalDateTime dataAssinatura;
    
    @NotNull(message = "Arquivo PDF é obrigatório")
    private MultipartFile arquivoPdf;
}
