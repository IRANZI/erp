package com.erp.Enterprise.Resource.Planning.service;

import com.erp.Enterprise.Resource.Planning.dto.AuthResponse;
import com.erp.Enterprise.Resource.Planning.dto.LoginRequest;
import com.erp.Enterprise.Resource.Planning.dto.UserSummaryResponse;
import com.erp.Enterprise.Resource.Planning.entity.UserAccount;
import com.erp.Enterprise.Resource.Planning.exception.ApiException;
import com.erp.Enterprise.Resource.Planning.repository.UserAccountRepository;
import com.erp.Enterprise.Resource.Planning.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;

    public AuthResponse login(LoginRequest request) {
        var authentication = authenticate(request);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserAccount account = userAccountRepository.findByEmployeeEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        String token = jwtService.generateToken(userDetails, account.getRoles());
        return new AuthResponse("Bearer", token, jwtService.getExpirationMinutes(), toSummary(account));
    }

    private org.springframework.security.core.Authentication authenticate(LoginRequest request) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials or email is not verified.");
        }
    }

    public UserSummaryResponse toSummary(UserAccount account) {
        return new UserSummaryResponse(
                account.getId(),
                account.getEmployee().getId(),
                account.getEmployee().getEmployeeCode(),
                account.getEmployee().getFullName(),
                account.getEmployee().getEmail(),
                account.getStatus(),
                account.getRoles()
        );
    }
}
