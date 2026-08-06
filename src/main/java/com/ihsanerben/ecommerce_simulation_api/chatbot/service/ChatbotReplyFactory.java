package com.ihsanerben.ecommerce_simulation_api.chatbot.service;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ChatbotReplyFactory {

    public String replyTo(String message) {
        String normalizedMessage = message.toLowerCase(Locale.forLanguageTag("tr-TR"));

        if (containsAny(normalizedMessage, "kargo", "teslimat", "ne zaman gelir")) {
            return "Siparişlerinizi Siparişlerim ekranından takip edebilirsiniz. Teslimat süresi satıcıya göre değişebilir.";
        }
        if (containsAny(normalizedMessage, "iade", "iptal", "geri gönder")) {
            return "Uygun durumdaki siparişinizi Siparişlerim ekranından iptal edebilirsiniz. İade süreci henüz demo kapsamındadır.";
        }
        if (containsAny(normalizedMessage, "sepet", "ürün ekle", "satın al")) {
            return "Ürünün yanındaki Sepete ekle butonunu kullanabilir, ardından Sepet ekranından sipariş oluşturabilirsiniz.";
        }
        if (containsAny(normalizedMessage, "şifre", "parola", "giriş")) {
            return "Giriş ekranındaki Parolamı unuttum bağlantısından parola yenileme işlemini başlatabilirsiniz.";
        }
        if (containsAny(normalizedMessage, "merhaba", "selam", "nasılsın")) {
            return "Merhaba! Ürün, sepet, sipariş, teslimat veya parola işlemleri hakkında yardımcı olabilirim.";
        }

        return "Bu konuda henüz ayrıntılı yanıt veremiyorum. Ürün, sepet, sipariş, teslimat, iade veya parola hakkında soru sorabilirsiniz.";
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
