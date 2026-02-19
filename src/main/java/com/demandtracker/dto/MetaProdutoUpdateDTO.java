package com.demandtracker.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaProdutoUpdateDTO {
    private Long projetoMetaId;

    @Size(max = 10, message = "Código deve ter no máximo 10 caracteres")
    private String codigo;

    @Size(max = 500, message = "Nome deve ter no máximo 500 caracteres")
    private String nome;

    private String descricao;

    @Size(max = 100, message = "Unidade de medida deve ter no máximo 100 caracteres")
    private String unidadeMedida;

    private Integer quantidade;

    private BigDecimal valorUnitario;

    private Integer inicio;

    private Integer fim;

    @Size(max = 1, message = "Status deve ter no máximo 1 caractere")
    private String status;

    private Integer percExecutado;
}
