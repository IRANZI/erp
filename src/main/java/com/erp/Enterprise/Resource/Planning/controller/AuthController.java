package com.erp.Enterprise.Resource.Planning.controller;

import com.erp.Enterprise.Resource.Planning.dto.AuthResponse;
import com.erp.Enterprise.Resource.Planning.dto.LoginRequest;
import com.erp.Enterprise.Resource.Planning.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
