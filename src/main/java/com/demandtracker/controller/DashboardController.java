package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
    
    @GetMapping("/demandas-por-projeto")
    public ResponseEntity<List<DemandaPorProjetoDTO>> getDemandasPorProjeto() {
        return ResponseEntity.ok(dashboardService.getDemandasPorProjeto());
    }
    
    @GetMapping("/demandas-por-status")
    public ResponseEntity<List<DemandaPorStatusDTO>> getDemandasPorStatus() {
        return ResponseEntity.ok(dashboardService.getDemandasPorStatus());
    }
}
