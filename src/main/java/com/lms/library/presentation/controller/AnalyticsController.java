package com.lms.library.presentation.controller;

import com.lms.library.application.dto.DashboardStatsResponse;
import com.lms.library.application.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(name = "Analytics", description = "Operations related to system statistics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        log.info("REST request to get dashboard statistics");
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }
}
