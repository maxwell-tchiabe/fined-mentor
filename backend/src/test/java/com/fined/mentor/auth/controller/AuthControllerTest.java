package com.fined.mentor.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fined.mentor.auth.dto.*;
import com.fined.mentor.auth.entity.Role;
import com.fined.mentor.auth.entity.User;
import com.fined.mentor.auth.service.AuthService;
import com.fined.mentor.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        Role role = new Role();
        role.setName(Role.RoleName.ROLE_USER);

        sampleUser = User.builder()
                .id("1")
                .username("testuser")
                .email("test@example.com")
                .roles(Set.of(role))
                .build();
    }

    @Test
    void registerUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        doNothing().when(authService).registerUser(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful. Please check your email for activation OTP."));
    }

    @Test
    void authenticateUser_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(sampleUser);
        
        ResponseCookie cookie = ResponseCookie.from("mentor-jwt", "sample-jwt").build();
        when(jwtService.generateJwtCookie(authentication)).thenReturn(cookie);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("mentor-jwt"))
                .andExpect(cookie().value("mentor-jwt", "sample-jwt"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void logoutUser_Success() throws Exception {
        ResponseCookie cookie = ResponseCookie.from("mentor-jwt", "").maxAge(0).build();
        when(jwtService.getCleanJwtCookie()).thenReturn(cookie);

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("mentor-jwt"))
                .andExpect(cookie().maxAge("mentor-jwt", 0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("You've been signed out!"));
    }

    @Test
    void activateUser_Success() throws Exception {
        ActivationRequest request = new ActivationRequest();
        request.setToken("123456");

        doNothing().when(authService).activateUser("123456");

        mockMvc.perform(post("/api/auth/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account activated successfully"));
    }

    @Test
    void resendActivation_Success() throws Exception {
        doNothing().when(authService).resendActivationToken("test@example.com");

        mockMvc.perform(post("/api/auth/resend-activation")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Activation OTP sent successfully"));
    }

    @Test
    void forgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        doNothing().when(authService).initiatePasswordReset("test@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset OTP sent to your email"));
    }

    @Test
    void resetPassword_Success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("123456");
        request.setNewPassword("newPassword123");

        doNothing().when(authService).resetPassword("123456", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    void getAuthenticatedUser_Success() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(sampleUser);

        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void getAuthenticatedUser_NotAuthenticated() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }

    @Test
    void getAuthenticatedUser_Anonymous() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }

    @Test
    void getAuthenticatedUser_Null() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }
}
