package com.ihsanerben.ecommerce_simulation_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendPasswordReset(String recipient, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject("E-Commerce password reset");
        message.setText("Use this link to reset your password. It expires in 15 minutes:\n" + resetLink);
        mailSender.send(message);
    }
}
