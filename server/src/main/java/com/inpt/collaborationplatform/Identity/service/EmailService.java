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

    @Value("${app.mail.from:no-reply@collaboration-platform.local}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

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

    public void sendResetCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your password reset code");
            message.setText(
                    "Your password reset code is: " + code + "\n\n" +
                            "This code expires in 10 minutes.\n" +
                            "If you didn't request a password reset, please ignore this email."
            );

            mailSender.send(message);
            log.info("Password reset code sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password reset code to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    public void sendProjectInvitation(String toEmail, String projectName, String invitedByEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("You're invited to join " + projectName);
            message.setText(
                    invitedByEmail + " invited you to join the project \"" + projectName + "\".\n\n" +
                            "Accept the invitation here:\n" +
                            frontendUrl + "/invitations/" + token + "\n\n" +
                            "If you do not have an account yet, register with this email first, then open the link again."
            );

            mailSender.send(message);
            log.info("Project invitation sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send project invitation to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send project invitation email");
        }
    }

    public void sendNotification(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Notification email sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send notification email to {}: {}", toEmail, e.getMessage());
        }
    }
}
