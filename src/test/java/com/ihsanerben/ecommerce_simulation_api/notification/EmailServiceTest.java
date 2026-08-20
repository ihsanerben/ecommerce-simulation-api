package com.ihsanerben.ecommerce_simulation_api.notification;

import com.ihsanerben.ecommerce_simulation_api.order.messaging.event.OrderItemSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final EmailService emailService = new EmailService(mailSender);

    @Test
    void sendsOrderConfirmationEmail() {
        emailService.sendOrderConfirmation("buyer@example.com", 10L, new BigDecimal("250.00"), 2);

        SimpleMailMessage message = capturedMessage();
        assertThat(message.getTo()).containsExactly("buyer@example.com");
        assertThat(message.getSubject()).isEqualTo("Siparişiniz alındı (#10)");
        assertThat(message.getText()).contains("Toplam: 250.00 TL");
    }

    @Test
    void sendsInvoiceEmailWithLineItems() {
        emailService.sendInvoice(
                "buyer@example.com",
                10L,
                new BigDecimal("250.00"),
                List.of(new OrderItemSnapshot(20L, "Keyboard", 2, new BigDecimal("125.00"), 4)));

        SimpleMailMessage message = capturedMessage();
        assertThat(message.getSubject()).isEqualTo("Faturanız (#10)");
        assertThat(message.getText()).contains("Keyboard - 2 x 125.00 TL = 250.00 TL");
    }

    @Test
    void sendsOrderPreparingEmail() {
        emailService.sendOrderPreparing("buyer@example.com", 10L);

        SimpleMailMessage message = capturedMessage();
        assertThat(message.getSubject()).isEqualTo("Siparişiniz hazırlanıyor (#10)");
        assertThat(message.getText()).contains("10 numaralı siparişiniz onaylandı");
    }

    @Test
    void sendsOrderCancelledEmail() {
        emailService.sendOrderCancelled("buyer@example.com", 10L);

        SimpleMailMessage message = capturedMessage();
        assertThat(message.getTo()).containsExactly("buyer@example.com");
        assertThat(message.getSubject()).isEqualTo("Siparişiniz iptal edildi (#10)");
        assertThat(message.getText()).contains("10 numaralı siparişiniz iptal edildi");
    }

    private SimpleMailMessage capturedMessage() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }
}
