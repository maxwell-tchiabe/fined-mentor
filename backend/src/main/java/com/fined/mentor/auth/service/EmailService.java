package com.fined.mentor.auth.service;

import com.fined.mentor.auth.exception.EmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${mailgun.api.key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${mailgun.from.email}")
    private String fromEmail;

    @Async
    public void sendActivationEmail(String toEmail, String username, String activationToken) {
        String subject = "Activate Your FinEd Mentor Account";
        String content = String.format(
                "Hello %s,\n\n" +
                        "Welcome to FinEd Mentor! Please use the following OTP to activate your account:\n\n" +
                        "OTP: %s\n\n" +
                        "This OTP will expire in 24 hours.\n\n" +
                        "If you didn't create an account, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "FinEd Mentor Team",
                username, activationToken);

        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        String subject = "Reset Your FinEd Mentor Password";
        String content = String.format(
                "Hello %s,\n\n" +
                        "We received a request to reset your password. Please use the following OTP to reset it:\n\n" +
                        "OTP: %s\n\n" +
                        "This OTP will expire in 1 hour.\n\n" +
                        "If you didn't request a password reset, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "FinEd Mentor Team",
                username, resetToken);

        sendEmail(toEmail, subject, content);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://api.mailgun.net/v3/" + domain + "/messages")
                    .basicAuth("api", apiKey)
                    .field("from", fromEmail)
                    .field("to", to)
                    .field("subject", subject)
                    .field("text", text)
                    .asJson();

            if (response.isSuccess()) {
                log.info("Email successfully sent to: {} via Mailgun. Response: {}", to, response.getBody());
            } else {
                log.error("Failed to send email to: {}. Status: {}, Error: {}", to, response.getStatus(),
                        response.getBody());
                throw new EmailException("Failed to send email via Mailgun");
            }
        } catch (Exception e) {
            log.error("Exception occurred while sending email to: {}", to, e);
            throw new EmailException("Exception occurred while sending email via Mailgun");
        }
    }
}
