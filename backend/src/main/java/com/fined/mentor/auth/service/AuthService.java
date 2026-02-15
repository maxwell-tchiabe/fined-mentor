package com.fined.mentor.auth.service;

import com.fined.mentor.auth.dto.RegisterRequest;
import com.fined.mentor.auth.exception.InvalidTokenException;
import com.fined.mentor.auth.exception.UserAlreadyActivatedException;
import com.fined.mentor.auth.exception.UserAlreadyExistsException;
import com.fined.mentor.auth.exception.UserNotFoundException;
import com.fined.mentor.auth.repository.RoleRepository;
import com.fined.mentor.auth.repository.UserRepository;
import com.fined.mentor.auth.entity.Role;
import com.fined.mentor.auth.entity.Token;
import com.fined.mentor.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        // Create new user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .activated(false)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Assign ROLE_USER by default
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.setRoles(Collections.singleton(userRole));

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Create and send activation token
        Token activationToken = tokenService.createActivationToken(savedUser);
        emailService.sendActivationEmail(savedUser.getEmail(), savedUser.getUsername(), activationToken.getToken());
    }

    @Transactional
    public void activateUser(String tokenValue) {
        Token token = tokenService.validateToken(tokenValue, Token.TokenType.ACTIVATION)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired activation token"));

        User user = token.getUser();
        user.setActivated(true);
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
        tokenService.markTokenAsUsed(token);

        log.info("User activated successfully: {}", user.getEmail());
    }

    public void resendActivationToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.isActivated()) {
            throw new UserAlreadyActivatedException("User is already activated");
        }

        Token activationToken = tokenService.createActivationToken(user);
        emailService.sendActivationEmail(user.getEmail(), user.getUsername(), activationToken.getToken());

        log.info("Activation token resent to: {}", user.getEmail());
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        Token resetToken = tokenService.createPasswordResetToken(user);
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetToken.getToken());

        log.info("Password reset initiated for: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        Token token = tokenService.validateToken(tokenValue, Token.TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
        tokenService.markTokenAsUsed(token);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }
}