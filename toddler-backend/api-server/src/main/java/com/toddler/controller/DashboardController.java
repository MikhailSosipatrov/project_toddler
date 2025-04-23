package com.toddler.controller;

import com.toddler.dto.DashboardDto;
import com.toddler.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<List<DashboardDto>> dashboardActive(HttpServletRequest request) {
        try {
            return ResponseEntity.ok().body(dashboardService.getDashboardActiveProjects(request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/archive")
    public ResponseEntity<List<DashboardDto>> dashboardArchive(HttpServletRequest request) {
        try {
            return ResponseEntity.ok().body(dashboardService.getDashboardArchiveProjects(request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
