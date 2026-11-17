package com.salessavvy.controller;

import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salessavvy.dto.DashboardResponse;
import com.salessavvy.service.AdminDashboardService;

@RestController
@RequestMapping("/api/admin/dashboard")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(
            AdminDashboardService adminDashboardService) {

        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {

        DashboardResponse response =
                adminDashboardService.getDashboardData();

        return ResponseEntity.ok(response);
    }
}