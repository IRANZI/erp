package com.erp.Enterprise.Resource.Planning.controller;

import com.erp.Enterprise.Resource.Planning.dto.RegisterRequest;
import com.erp.Enterprise.Resource.Planning.dto.RegisterResponse;
import com.erp.Enterprise.Resource.Planning.dto.ResendOtpRequest;
import com.erp.Enterprise.Resource.Planning.dto.VerifyEmailRequest;
import com.erp.Enterprise.Resource.Planning.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {
    private final RegistrationService registrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return registrationService.register(request);
    }

    @PostMapping("/verify-email")
    RegisterResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return registrationService.verifyEmail(request);
    }

    @PostMapping("/resend-otp")
    RegisterResponse resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return registrationService.resendOtp(request);
    }
}
