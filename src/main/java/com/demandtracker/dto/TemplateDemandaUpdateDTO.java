package com.demandtracker.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para atualização de TemplateDemanda
 */
@Data
public class TemplateDemandaUpdateDTO {
    
    private String tipo;
    
    private MultipartFile arquivoDocx;
}
