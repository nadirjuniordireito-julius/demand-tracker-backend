package com.demandtracker.controller;

import com.demandtracker.dto.BubbleNodeDto;
import com.demandtracker.service.TedHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ted")
@RequiredArgsConstructor
public class TedHealthController {

    private final TedHealthService tedHealthService;

    @GetMapping("/{id}/health")
    public ResponseEntity<BubbleNodeDto> getHealth(@PathVariable Long id) {
        return ResponseEntity.ok(tedHealthService.calculateHealth(id));
    }
}
