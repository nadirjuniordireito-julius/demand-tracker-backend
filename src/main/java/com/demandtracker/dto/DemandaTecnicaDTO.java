package com.demandtracker.dto;

import com.demandtracker.entity.DemandaTecnica;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaTecnicaDTO {
    private Long id;
    private Long projetoId;
    private String codigo;
    private String nome;
    private LocalDateTime dataAbertura;
    private Long usuarioId;
    private ProjetoDTO projeto;
    private UsuarioDTO usuario;
    private TermoAberturaDTO termoAbertura;
    private TermoPlanejamentoDTO termoPlanejamento;
    private TermoEncerramentoDTO termoEncerramento;
    private String status;
    
    public static DemandaTecnicaDTO fromEntity(DemandaTecnica demanda) {
        DemandaTecnicaDTO dto = new DemandaTecnicaDTO();
        dto.setId(demanda.getId());
        dto.setProjetoId(demanda.getProjeto().getId());
        dto.setCodigo(demanda.getCodigo());
        dto.setNome(demanda.getNome());
        dto.setDataAbertura(demanda.getDataAbertura());
        dto.setUsuarioId(demanda.getUsuario().getId());
        
        if (demanda.getProjeto() != null) {
            dto.setProjeto(ProjetoDTO.fromEntity(demanda.getProjeto()));
        }
        if (demanda.getUsuario() != null) {
            dto.setUsuario(UsuarioDTO.fromEntity(demanda.getUsuario()));
        }
        if (demanda.getTermoAbertura() != null) {
            dto.setTermoAbertura(TermoAberturaDTO.fromEntity(demanda.getTermoAbertura()));
        }
        if (demanda.getTermoPlanejamento() != null) {
            dto.setTermoPlanejamento(TermoPlanejamentoDTO.fromEntity(demanda.getTermoPlanejamento()));
        }
        if (demanda.getTermoEncerramento() != null) {
            dto.setTermoEncerramento(TermoEncerramentoDTO.fromEntity(demanda.getTermoEncerramento()));
        }
        
        // Calcula status
        if (demanda.getTermoEncerramento() != null && demanda.getTermoEncerramento().getDataAssinatura() != null) {
            dto.setStatus("closed");
        } else if (demanda.getTermoPlanejamento() != null && demanda.getTermoPlanejamento().getDataAssinatura() != null) {
            dto.setStatus("inExecution");
        } else if (demanda.getTermoAbertura() != null && demanda.getTermoAbertura().getDataAssinatura() != null) {
            dto.setStatus("inPlanning");
        } else {
            dto.setStatus("opened");
        }
        
        return dto;
    }
}
