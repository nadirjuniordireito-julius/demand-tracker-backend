package com.demandtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaProdutoCreateDTO {
    @NotNull(message = "ID da meta do projeto é obrigatório")
    private Long projetoMetaId;

    @NotBlank(message = "Código é obrigatório")
    @Size(max = 10, message = "Código deve ter no máximo 10 caracteres")
    private String codigo;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 500, message = "Nome deve ter no máximo 500 caracteres")
    private String nome;

    private String descricao;

    @NotBlank(message = "Unidade de medida é obrigatória")
    @Size(max = 100, message = "Unidade de medida deve ter no máximo 100 caracteres")
    private String unidadeMedida;

    @NotNull(message = "Quantidade é obrigatória")
    private Integer quantidade;

    @NotNull(message = "Valor unitário é obrigatório")
    private BigDecimal valorUnitario;

    @NotNull(message = "Início é obrigatório")
    private Integer inicio;

    @NotNull(message = "Fim é obrigatório")
    private Integer fim;

    @NotBlank(message = "Status é obrigatório")
    @Size(max = 1, message = "Status deve ter no máximo 1 caractere")
    private String status;

    private Integer percExecutado;
}
