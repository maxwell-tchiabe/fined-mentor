package com.fined.mentor.auth.controller;

import com.fined.mentor.auth.dto.*;
import com.fined.mentor.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration request for user: {}", registerRequest.getEmail());

        authService.registerUser(registerRequest);

        return ResponseEntity.ok(ApiResponse.success(
                "Registration successful. Please check your email for activation OTP."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request for: {}", loginRequest.getUsernameOrEmail());

        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);

        return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse> activateUser(@Valid @RequestBody ActivationRequest activationRequest) {
        log.info("Account activation request with token");

        authService.activateUser(activationRequest.getToken());

        return ResponseEntity.ok(ApiResponse.success("Account activated successfully"));
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<ApiResponse> resendActivation(@RequestParam String email) {
        log.info("Resend activation request for: {}", email);

        authService.resendActivationToken(email);

        return ResponseEntity.ok(ApiResponse.success("Activation OTP sent successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for: {}", request.getEmail());

        authService.initiatePasswordReset(request.getEmail());

        return ResponseEntity.ok(ApiResponse.success("Password reset OTP sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Reset password request with token");

        authService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }
}