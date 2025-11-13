// package io.mawhebty.services;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;
// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class EmailService {

//     @Value("${spring.mail.username}")
//     private String emailSender;

//     private final JavaMailSender mailSender;
    
//     @Async
//     public void sendVerificationEmail(String email, String code) {
//         SimpleMailMessage message = new SimpleMailMessage();
//         message.setFrom(emailSender);
//         message.setTo(email);
//         message.setSubject("Talent Scout OTP Code");
//         message.setText("Use this code to verify your account: " + code);
//         mailSender.send(message);
//     }

// }


package io.mawhebty.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String emailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.name:mawhebty}")
    private String appName;

    private final JavaMailSender mailSender;

    @PostConstruct
    public void init() {
        if (isEmailConfigured()) {
            log.info("Email Service initialized successfully");
        } else {
            log.warn("Email Service not configured - EmailService features disabled");
        }
    }

    /**
     * Send OTP verification email (HTML version)
     */
    @Async
    public void sendVerificationEmail(String email, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(emailSender, appName);
            helper.setTo(email);
            helper.setSubject("Verify Your Mawhebty Account");

            String htmlContent = buildVerificationEmailHtml(code);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Verification email sent to: {}", email);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage());
            // Fallback to simple email
            sendSimpleVerificationEmail(email, code);
        }
    }

    /**
     * Fallback simple email
     */
    @Async
    public void sendSimpleVerificationEmail(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailSender);
            message.setTo(email);
            message.setSubject("Mawhebty - Verify Your Account");
            message.setText(
                "Welcome to Mawhebty Platform!\n\n" +
                "Your verification code is: " + code + "\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Mawhebty Platform Team"
            );

            mailSender.send(message);
            log.info("Simple verification email sent to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send simple verification email: {}", e.getMessage());
        }
    }

    /**
     * Send welcome email after successful verification
     */
    @Async
    public void sendWelcomeEmail(String email, String fullName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(emailSender, appName);
            helper.setTo(email);
            helper.setSubject("Welcome to Mawhebty!");

            String htmlContent = buildWelcomeEmailHtml(fullName);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("✅ Welcome email sent to: {}", email);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
    }

    /**
     * Send account approval email
     */
    @Async
    public void sendAccountApprovedEmail(String email, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailSender);
            message.setTo(email);
            message.setSubject("Your Mawhebty Account is Approved!");
            message.setText(
                "Hello " + fullName + ",\n\n" +
                "Great news! Your Mawhebty account has been approved.\n\n" +
                "You can now login and start exploring talents and opportunities.\n\n" +
                "Get started: " + frontendUrl + "/login\n\n" +
                "Best regards,\n" +
                "Mawhebty Platform Team"
            );

            mailSender.send(message);
            log.info("Account approved email sent to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send account approved email: {}", e.getMessage());
        }
    }

    /**
     * Build HTML content for verification email
     */
    private String buildVerificationEmailHtml(String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; }
                    .header { text-align: center; color: #333; }
                    .code { font-size: 32px; font-weight: bold; color: #2563eb; text-align: center; margin: 30px 0; }
                    .footer { margin-top: 30px; font-size: 12px; color: #666; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Mawhebty</h1>
                        <h3>Verify Your Account</h3>
                    </div>
            
                    <p>Hello,</p>
            
                    <p>Thank you for joining Mawhebty Platform! Use the verification code below to complete your registration:</p>
            
                    <div class="code">%s</div>
            
                    <p>This code will expire in <strong>10 minutes</strong>.</p>
            
                    <p>If you didn't request this verification, please ignore this email.</p>
            
                    <div class="footer">
                        <p>Best regards,<br>Mawhebty Platform Team</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(code);
    }

    /**
     * Build HTML content for welcome email
     */
    private String buildWelcomeEmailHtml(String fullName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; }
                    .header { text-align: center; color: #333; }
                    .button { display: inline-block; padding: 12px 24px; background: #2563eb; color: white; 
                              text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { margin-top: 30px; font-size: 12px; color: #666; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎭 Mawhebty</h1>
                        <h2>Welcome to Our Community!</h2>
                    </div>
                    
                    <p>Hello <strong>%s</strong>,</p>
                    
                    <p>Welcome to Mawhebty Platform! Your account has been successfully verified and is now active.</p>
                    
                    <p>🚀 <strong>What you can do now:</strong></p>
                    <ul>
                        <li>Browse talented individuals</li>
                        <li>Connect with researchers and investors</li>
                        <li>Showcase your skills and projects</li>
                        <li>Explore collaboration opportunities</li>
                    </ul>
                    
                    <p style="text-align: center;">
                        <a href="%s/login" class="button">Get Started</a>
                    </p>
                    
                    <p>If you have any questions, feel free to reply to this email.</p>
                    
                    <div class="footer">
                        <p>Best regards,<br>Mawhebty Platform Team</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, frontendUrl);
    }

    /**
     * Check if email service is configured
     */
    public boolean isEmailConfigured() {
        return emailSender != null && !emailSender.trim().isEmpty();
    }
}