package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoEncerramentoDocValidacaoHashDTO {
    private Long documentoId;
    private Boolean valido;
    private String hashInformado;
    private String hashCalculado;
}
