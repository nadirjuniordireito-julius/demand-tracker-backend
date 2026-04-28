package com.demandtracker.controller;

import com.demandtracker.dto.DemandaExecucaoCreateDTO;
import com.demandtracker.dto.DemandaExecucaoDTO;
import com.demandtracker.dto.DemandaExecucaoGanttDTO;
import com.demandtracker.dto.DemandaExecucaoPerfilCheckDTO;
import com.demandtracker.dto.DemandaExecucaoUpdateDTO;
import com.demandtracker.dto.ExecucaoProfissionalDTO;
import com.demandtracker.dto.TermoEncerramentoDTO;
import com.demandtracker.service.DemandaExecucaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandas-execucao")
@RequiredArgsConstructor
public class DemandaExecucaoController {

    private final DemandaExecucaoService service;

    @GetMapping
    public ResponseEntity<Page<DemandaExecucaoDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandaExecucaoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/demanda/{demandaTecnicaId}")
    public ResponseEntity<DemandaExecucaoDTO> findByDemandaTecnicaId(@PathVariable Long demandaTecnicaId) {
        return ResponseEntity.ok(service.findByDemandaTecnicaId(demandaTecnicaId));
    }

    /**
     * Dados para o frontend montar o Gantt da execução.
     * GET /api/demandas-execucao/gantt/demanda/{demandaTecnicaId}
     */
    @GetMapping("/gantt/demanda/{demandaTecnicaId}")
    public ResponseEntity<DemandaExecucaoGanttDTO> getGanttByDemandaTecnicaId(@PathVariable Long demandaTecnicaId) {
        return ResponseEntity.ok(service.getGanttByDemandaTecnicaId(demandaTecnicaId));
    }

    @GetMapping("/analytics/profissional/{profissionalId}")
    public ResponseEntity<List<ExecucaoProfissionalDTO>> getExecucaoProfissional(@PathVariable Long profissionalId) {
        return ResponseEntity.ok(service.getExecucaoProfissional(profissionalId));
    }

    /**
     * Check de horas planejadas por perfil de uma Demanda Técnica.
     * Compara A) horas planejadas no Termo de Planejamento (TermoPlanejamentoCusto)
     * com B) horas planejadas na execução (DemandaExecucaoTarefaRecurso).
     *
     * GET /api/demandas-execucao/check/perfis/{demandaTecnicaId}
     */
    @GetMapping("/check/perfis/{demandaTecnicaId}")
    public ResponseEntity<List<DemandaExecucaoPerfilCheckDTO>> getCheckHorasPorPerfil(@PathVariable Long demandaTecnicaId) {
        return ResponseEntity.ok(service.getCheckHorasPorPerfil(demandaTecnicaId));
    }

    @PostMapping
    public ResponseEntity<DemandaExecucaoDTO> create(@Valid @RequestBody DemandaExecucaoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemandaExecucaoDTO> update(@PathVariable Long id, @Valid @RequestBody DemandaExecucaoUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para encerrar a execução da demanda e gerar o Termo de Encerramento automaticamente.
     * Recebe o ID da DemandaExecucao (path) e o ID do usuário que está encerrando (query param).
     *
     * Ex: POST /api/demandas-execucao/3/encerrar?usuarioId=1
     */
    @PostMapping("/{id}/encerrar")
    public ResponseEntity<TermoEncerramentoDTO> encerrarExecucao(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(service.encerrarExecucaoInterna(id, usuarioId));
    }

    /**
     * Reabre uma execução previamente encerrada (status "CONCLUIDA"), revertendo
     * a Demanda Técnica para "E" (Em execução) e a execução para "EM_ANDAMENTO".
     *
     * Bloqueado quando a demanda está com status "G" (Termo de Encerramento assinado).
     *
     * Ex: POST /api/demandas-execucao/3/reabrir
     */
    @PostMapping("/{id}/reabrir")
    public ResponseEntity<DemandaExecucaoDTO> reabrirExecucao(@PathVariable Long id) {
        return ResponseEntity.ok(service.reabrirExecucao(id));
    }
}
