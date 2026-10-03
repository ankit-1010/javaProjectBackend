package com.moneymate.controller;

import com.moneymate.dto.AdminStatsDto;
import com.moneymate.dto.ApiResponse;
import com.moneymate.dto.ChangePasswordRequest;
import com.moneymate.dto.UserOverviewDto;
import com.moneymate.service.AdminService;
import com.moneymate.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getAdminStats() {
        return ResponseEntity.ok(adminService.getAdminStats());
    }

    @GetMapping("/user-overview/{userId}")
    public ResponseEntity<UserOverviewDto> getUserOverview(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserOverview(userId));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changeAdminPassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changeAdminPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Admin password updated successfully"));
    }
}
