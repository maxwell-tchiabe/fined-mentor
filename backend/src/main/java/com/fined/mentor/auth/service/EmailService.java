package com.fined.mentor.auth.service;

import com.fined.mentor.auth.exception.EmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendActivationEmail(String toEmail, String username, String activationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Activate Your FinEd Mentor Account");
            message.setText(
                    String.format(
                            "Hello %s,\n\n" +
                                    "Welcome to FinEd Mentor! Please use the following OTP to activate your account:\n\n"
                                    +
                                    "OTP: %s\n\n" +
                                    "This OTP will expire in 24 hours.\n\n" +
                                    "If you didn't create an account, please ignore this email.\n\n" +
                                    "Best regards,\n" +
                                    "FinEd Mentor Team",
                            username, activationToken));

            mailSender.send(message);
            log.info("Activation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send activation email to: {}", toEmail, e);
            throw new EmailException("Failed to send activation email");
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Reset Your FinEd Mentor Password");
            message.setText(
                    String.format(
                            "Hello %s,\n\n" +
                                    "We received a request to reset your password. Please use the following OTP to reset it:\n\n"
                                    +
                                    "OTP: %s\n\n" +
                                    "This OTP will expire in 1 hour.\n\n" +
                                    "If you didn't request a password reset, please ignore this email.\n\n" +
                                    "Best regards,\n" +
                                    "FinEd Mentor Team",
                            username, resetToken));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new EmailException("Failed to send password reset email");
        }
    }
}