package com.demandtracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * DTO para criação de TermoEncerramentoDoc
 */
@Data
public class TermoEncerramentoDocCreateDTO {
    
    @NotNull(message = "ID do Termo de Encerramento é obrigatório")
    private Long termoEncerramentoId;
    
    private LocalDateTime dataAssinatura;
    
    @NotNull(message = "Arquivo PDF é obrigatório")
    private MultipartFile arquivoPdf;
}
