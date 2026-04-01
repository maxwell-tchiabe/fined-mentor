package com.fined.mentor.auth.service;

import com.fined.mentor.auth.dto.RegisterRequest;
import com.fined.mentor.auth.entity.Role;
import com.fined.mentor.auth.entity.Token;
import com.fined.mentor.auth.entity.User;
import com.fined.mentor.auth.exception.InvalidTokenException;
import com.fined.mentor.auth.exception.UserAlreadyActivatedException;
import com.fined.mentor.auth.exception.UserAlreadyExistsException;
import com.fined.mentor.auth.exception.UserNotFoundException;
import com.fined.mentor.auth.repository.RoleRepository;
import com.fined.mentor.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private Token sampleToken;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id("1")
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .activated(false)
                .build();

        sampleToken = Token.builder()
                .token("123456")
                .user(sampleUser)
                .type(Token.TokenType.ACTIVATION)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
    }

    @Test
    void registerUser_Success() {
        Role userRole = new Role();
        userRole.setName(Role.RoleName.ROLE_USER);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenService.createActivationToken(any(User.class))).thenReturn(sampleToken);

        assertDoesNotThrow(() -> authService.registerUser(registerRequest));

        verify(userRepository).save(any(User.class));
        verify(emailService).sendActivationEmail(eq("test@example.com"), eq("testuser"), eq("123456"));
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void activateUser_Success() {
        when(tokenService.validateToken("123456", Token.TokenType.ACTIVATION)).thenReturn(Optional.of(sampleToken));

        assertDoesNotThrow(() -> authService.activateUser("123456"));

        assertTrue(sampleUser.isActivated());
        verify(userRepository).save(sampleUser);
        verify(tokenService).markTokenAsUsed(sampleToken);
    }

    @Test
    void activateUser_InvalidToken() {
        when(tokenService.validateToken("123456", Token.TokenType.ACTIVATION)).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> authService.activateUser("123456"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resendActivationToken_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(tokenService.createActivationToken(sampleUser)).thenReturn(sampleToken);

        assertDoesNotThrow(() -> authService.resendActivationToken("test@example.com"));

        verify(emailService).sendActivationEmail(eq("test@example.com"), eq("testuser"), eq("123456"));
    }

    @Test
    void resendActivationToken_UserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.resendActivationToken("test@example.com"));
    }

    @Test
    void resendActivationToken_UserAlreadyActivated() {
        sampleUser.setActivated(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        assertThrows(UserAlreadyActivatedException.class, () -> authService.resendActivationToken("test@example.com"));
    }

    @Test
    void initiatePasswordReset_Success() {
        sampleToken.setType(Token.TokenType.PASSWORD_RESET);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(tokenService.createPasswordResetToken(sampleUser)).thenReturn(sampleToken);

        assertDoesNotThrow(() -> authService.initiatePasswordReset("test@example.com"));

        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), eq("testuser"), eq("123456"));
    }

    @Test
    void resetPassword_Success() {
        sampleToken.setType(Token.TokenType.PASSWORD_RESET);
        when(tokenService.validateToken("123456", Token.TokenType.PASSWORD_RESET)).thenReturn(Optional.of(sampleToken));
        when(passwordEncoder.encode("newPassword")).thenReturn("new_encoded_password");

        assertDoesNotThrow(() -> authService.resetPassword("123456", "newPassword"));

        assertEquals("new_encoded_password", sampleUser.getPassword());
        verify(userRepository).save(sampleUser);
        verify(tokenService).markTokenAsUsed(sampleToken);
    }
    @Test
    void registerUser_RoleNotFound_ThrowsRuntimeException() {
        lenient().when(userRepository.existsByUsername(anyString())).thenReturn(false);
        lenient().when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.registerUser(registerRequest));
    }

    @Test
    void initiatePasswordReset_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.initiatePasswordReset("unknown@example.com"));
    }

    @Test
    void resetPassword_InvalidToken_ThrowsException() {
        when(tokenService.validateToken(anyString(), any())).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> authService.resetPassword("invalid", "newPass"));
    }
}
