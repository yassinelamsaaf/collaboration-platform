package com.inpt.collaborationplatform.Identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your verification code");
            message.setText(
                    "Your verification code is: " + code + "\n\n" +
                            "This code expires in 10 minutes.\n" +
                            "If you didn't request this, please ignore this email."
            );

            mailSender.send(message);
            log.info("Verification code sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send verification code to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }
}