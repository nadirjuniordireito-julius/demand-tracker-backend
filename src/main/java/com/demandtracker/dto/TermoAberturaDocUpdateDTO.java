package com.demandtracker.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * DTO para atualização de TermoAberturaDoc
 */
@Data
public class TermoAberturaDocUpdateDTO {
    
    private LocalDateTime dataAssinatura;
    
    private MultipartFile arquivoPdf;
}
