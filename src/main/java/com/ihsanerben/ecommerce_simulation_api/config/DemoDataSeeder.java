package com.ihsanerben.ecommerce_simulation_api.config;

import com.ihsanerben.ecommerce_simulation_api.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.repository.CategoryRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data-enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {
    private final CategoryRepository categories;
    private final ProductRepository products;

    @Override @Transactional
    public void run(ApplicationArguments args) {
        Category tech = category("Teknoloji", "Günlük hayatı kolaylaştıran teknoloji ürünleri");
        Category fashion = category("Moda", "Sezonun öne çıkan parçaları");
        Category home = category("Ev & Yaşam", "Evin için modern seçimler");
        Category care = category("Kişisel Bakım", "Kendine ayırdığın zamanı güzelleştiren ürünler");
        List.of(
                item("Kablosuz Kulaklık Pro", "Aktif gürültü engelleme ve uzun pil ömrü", "2499.90", 28, tech),
                item("Akıllı Saat Fit", "Sağlık takibi ve şık tasarım", "1899.90", 34, tech),
                item("Ultra İnce Laptop", "Güçlü performans, hafif gövde", "24999.00", 9, tech),
                item("Kompakt Fotoğraf Makinesi", "Anılarını yüksek çözünürlükte yakala", "8499.50", 13, tech),
                item("Günlük Sneaker", "Konforlu ve zamansız şehir stili", "1599.90", 42, fashion),
                item("Polarize Güneş Gözlüğü", "UV400 korumalı modern çerçeve", "899.90", 30, fashion),
                item("Şehir Sırt Çantası", "Laptop bölmeli suya dayanıklı tasarım", "1199.00", 25, fashion),
                item("Minimal Kol Saati", "Paslanmaz çelik kasa ve deri kordon", "2199.00", 18, fashion),
                item("Seramik Kahve Seti", "Dört kişilik el yapımı görünümlü set", "749.90", 36, home),
                item("Modern Masa Lambası", "Sıcak ışıklı sade çalışma lambası", "1099.90", 21, home),
                item("Aromaterapi Difüzörü", "Sessiz çalışma ve renkli ambiyans", "649.90", 39, home),
                item("Çiçeksi Parfüm", "Gün boyu kalıcı zarif koku", "1399.90", 26, care),
                item("Doğal Bakım Seti", "Cilt bakım rutini için dört parça", "999.90", 31, care),
                item("Profesyonel Saç Kurutma", "Hızlı kurutma ve iyon teknolojisi", "1799.90", 17, care)
        ).forEach(p -> { if (!products.existsByName(p.getName())) products.save(p); });
    }

    private Category category(String name, String description) {
        return categories.findByName(name).orElseGet(() -> categories.save(
                Category.builder().name(name).description(description).build()));
    }

    private Product item(String name, String description, String price, int stock, Category category) {
        LocalDateTime now = LocalDateTime.now();
        return Product.builder().name(name).description(description).price(new BigDecimal(price))
                .stockQuantity(stock).category(category).createdAt(now).updatedAt(now).build();
    }
}
