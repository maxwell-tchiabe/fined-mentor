package com.fined.mentor.auth.controller;

import com.fined.mentor.auth.dto.*;
import com.fined.mentor.auth.entity.User;
import com.fined.mentor.auth.service.AuthService;
import com.fined.mentor.auth.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResponseCookie jwtCookie = jwtService.generateJwtCookie(authentication);

        User user = (User) authentication.getPrincipal();
        List<String> roles = user.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        JwtResponse jwtResponse = new JwtResponse(null, user.getId(), user.getUsername(), user.getEmail(), roles);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(ApiResponse.success("Login successful", jwtResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser() {
        ResponseCookie cookie = jwtService.getCleanJwtCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("You've been signed out!"));
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