package com.ihsanerben.ecommerce_simulation_api.chatbot.service;

import com.ihsanerben.ecommerce_simulation_api.chatbot.dto.ChatbotReply;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ChatbotReplyFactory {

    public String replyTo(String message) {
        return createReply(message).message();
    }

    public ChatbotReply createReply(String message) {
        String normalizedMessage = message.toLowerCase(Locale.forLanguageTag("tr-TR"));

        if (containsAny(normalizedMessage, "kargo", "teslimat", "ne zaman gelir")) {
            return matchedReply(
                    "DELIVERY",
                    "Siparişlerinizi Siparişlerim ekranından takip edebilirsiniz. Teslimat süresi satıcıya göre değişebilir.");
        }
        if (containsAny(normalizedMessage, "iade", "iptal", "geri gönder")) {
            return matchedReply(
                    "RETURN",
                    "Uygun durumdaki siparişinizi Siparişlerim ekranından iptal edebilirsiniz. İade süreci henüz demo kapsamındadır.");
        }
        if (containsAny(normalizedMessage, "sepet", "ürün ekle", "satın al", "satin al")) {
            return matchedReply(
                    "CART",
                    "Ürünün yanındaki Sepete ekle butonunu kullanabilir, ardından Sepet ekranından sipariş oluşturabilirsiniz.");
        }
        if (containsAny(normalizedMessage, "şifre", "parola", "giriş", "giris")) {
            return matchedReply(
                    "PASSWORD",
                    "Giriş ekranındaki Parolamı unuttum bağlantısından parola yenileme işlemini başlatabilirsiniz.");
        }
        if (containsAny(normalizedMessage, "merhaba", "selam", "nasılsın", "nasilsin")) {
            return matchedReply(
                    "GREETING",
                    "Merhaba! Ürün, sepet, sipariş, teslimat veya parola işlemleri hakkında yardımcı olabilirim.");
        }

        return new ChatbotReply(
                "Bu konuda henüz ayrıntılı yanıt veremiyorum. Ürün, sepet, sipariş, teslimat, iade veya parola hakkında soru sorabilirsiniz.",
                "UNKNOWN",
                false);
    }

    private ChatbotReply matchedReply(String category, String message) {
        return new ChatbotReply(message, category, true);
    }

    private boolean containsAny(String message, String... terms) {
        for (String term : terms) {
            if (message.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
