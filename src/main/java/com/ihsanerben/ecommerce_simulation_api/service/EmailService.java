package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.messaging.event.OrderItemSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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

    public void sendOrderConfirmation(String recipient, Long orderId, BigDecimal totalAmount, int itemCount) {
        send(
                recipient,
                "Siparişiniz alındı (#%d)".formatted(orderId),
                "Siparişiniz başarıyla alındı.\n\nSipariş no: %d\nÜrün sayısı: %d\nToplam: %s TL"
                        .formatted(orderId, itemCount, totalAmount));
    }

    public void sendInvoice(String recipient, Long orderId, BigDecimal totalAmount, List<OrderItemSnapshot> items) {
        StringBuilder invoice = new StringBuilder("FATURA\n\nSipariş no: ")
                .append(orderId)
                .append("\n\n");

        for (OrderItemSnapshot item : items) {
            BigDecimal lineTotal = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
            invoice.append(item.productName())
                    .append(" - ")
                    .append(item.quantity())
                    .append(" x ")
                    .append(item.unitPrice())
                    .append(" TL = ")
                    .append(lineTotal)
                    .append(" TL\n");
        }

        invoice.append("\nToplam: ").append(totalAmount).append(" TL");
        send(recipient, "Faturanız (#%d)".formatted(orderId), invoice.toString());
    }

    public void sendOrderPreparing(String recipient, Long orderId) {
        send(
                recipient,
                "Siparişiniz hazırlanıyor (#%d)".formatted(orderId),
                "%d numaralı siparişiniz onaylandı ve hazırlanıyor.".formatted(orderId));
    }

    public void sendOrderCancelled(String recipient, Long orderId) {
        send(
                recipient,
                "Siparişiniz iptal edildi (#%d)".formatted(orderId),
                "%d numaralı siparişiniz iptal edildi.".formatted(orderId));
    }

    private void send(String recipient, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
