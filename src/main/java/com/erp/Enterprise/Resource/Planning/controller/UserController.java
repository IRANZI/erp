package com.erp.Enterprise.Resource.Planning.controller;

import com.erp.Enterprise.Resource.Planning.dto.UpdateUserRolesRequest;
import com.erp.Enterprise.Resource.Planning.dto.UpdateUserStatusRequest;
import com.erp.Enterprise.Resource.Planning.dto.UserSummaryResponse;
import com.erp.Enterprise.Resource.Planning.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserManagementService userManagementService;

    @GetMapping
    List<UserSummaryResponse> listUsers() {
        return userManagementService.listUsers();
    }

    @PatchMapping("/{id}/status")
    UserSummaryResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return userManagementService.updateStatus(id, request.status());
    }

    @PatchMapping("/{id}/roles")
    UserSummaryResponse updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
        return userManagementService.updateRoles(id, request.roles());
    }
}
