package com.demandtracker.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * DTO para atualização de TermoPlanejamentoDoc
 */
@Data
public class TermoPlanejamentoDocUpdateDTO {
    
    private LocalDateTime dataAssinatura;
    
    private MultipartFile arquivoPdf;
}
