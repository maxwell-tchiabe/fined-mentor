package com.fined.mentor.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivationRequest {
    @NotBlank(message = "OTP token is required")
    private String token;
}