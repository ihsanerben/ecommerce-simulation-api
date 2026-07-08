# E-Ticaret Simülasyonu — Staj Projesi Rehberin

**Proje Adı:** E-Commerce Simulation API
**Süre:** 8 hafta (2 ay)
**Doküman Versiyonu:** 1.0

---

Merhaba ve aramıza hoş geldin! 👋

Bu doküman, staj sürecin boyunca üzerinde çalışacağın projenin rehberi. Amacımız sana sadece bir "ödev" vermek değil; gerçek bir backend geliştiricinin günlük hayatında karşılaştığı problemleri, kararları ve alışkanlıkları birlikte deneyimlemeni sağlamak. Merak etme, her şeyi ilk günden bilmen beklenmiyor — bu proje tam da öğrenerek ilerlemen için tasarlandı.

Aşağıda ne yapacağını, hangi teknolojileri kullanacağını ve haftalık olarak nereye ulaşmanı beklediğimizi bulacaksın. Takıldığın her noktada soru sormaktan çekinme; bu süreçte yanındayız.

---

## 1. Ne Yapacaksın?

Bir e-ticaret platformunun temel akışlarını (kullanıcı yönetimi, ürün listeleme, sepet, sipariş oluşturma) baştan sona sen tasarlayıp geliştireceksin. Yani gerçek bir alışveriş sitesinin arka planında dönen mantığın küçük ama eksiksiz bir versiyonunu yazacaksın.

Staj sonunda hedefimiz şu: Tek başına tasarlayabildiğin, test ettiğin, dokümante ettiğin ve güvenli bir REST API'yi bize gururla gösterebilmen. Bu proje boyunca katmanlı mimari, RESTful API tasarımı, veritabanı ilişkileri, güvenlik ve test yazımı gibi konularda ciddi bir tecrübe kazanacaksın.

---

## 2. Kullanacağın Teknolojiler (Tech Stack)

Aşağıdaki teknolojilerle çalışacaksın. Bazılarını hiç kullanmadıysan endişelenme — öğrenmen de sürecin bir parçası.

| Kategori | Teknoloji |
|---|---|
| Programlama Dili | Java (17+) |
| Framework | Spring Boot (3.x) |
| Veritabanı | PostgreSQL |
| ORM | Spring Data JPA |
| API Dokümantasyonu | Swagger (springdoc-openapi) |
| Kimlik Doğrulama | JWT (JSON Web Token) |
| Unit & Integration Test | JUnit 5, Mockito |
| Test Assertion | AssertJ |
| Boilerplate Azaltma | Lombok |
| DTO Doğrulama | Jakarta Bean Validation (`jakarta.validation`) |
| Build Aracı | Maven |
| Versiyon Kontrol | Git |

---

## 3. Mimariden Ne Bekliyoruz?

Projeni **katmanlı mimari (layered architecture)** prensiplerine uygun geliştirmeni istiyoruz. Bu, kodunu düzenli ve bakımı kolay tutmanın en temel yollarından biri:

```
Controller  ->  Service  ->  Repository  ->  Database
     |            |
   DTO      İş Mantığı
```

Her katmanın bir sorumluluğu var, bunları birbirine karıştırmamaya dikkat et:

- **Controller:** Sadece HTTP isteklerini karşılar, DTO alır/döner. Buraya iş mantığı **yazma**.
- **Service:** Tüm iş mantığın burada olacak. Transaction yönetimini de bu katmanda yapacaksın.
- **Repository:** Veritabanı erişim katmanı (Spring Data JPA interface'leri).
- **Entity:** Veritabanı tablolarını temsil eden JPA sınıfların.
- **DTO:** İstek (Request) ve yanıt (Response) nesneleri. Entity'lerini **asla** doğrudan API'de dönme.
- **Mapper:** Entity <-> DTO dönüşümleri (manuel veya MapStruct).

**Aklında tutman gereken üç altın kural:**
1. Entity nesnelerin hiçbir zaman controller katmanından dışarıya sızmasın.
2. İş mantığını controller içine yazma.
3. Katmanlar arası bağımlılık tek yönlü olsun (Controller → Service → Repository).

---

## 4. Veritabanı Modelin

Aşağıdaki entity'leri oluşturacaksın. İlişkileri (`@OneToMany`, `@ManyToOne`, `@ManyToMany`) doğru kurmaya özen göster — bu, projenin bel kemiği.

### 4.1 User (Kullanıcı)
| Alan | Tip | Açıklama |
|---|---|---|
| id | Long | Primary key |
| username | String | Benzersiz |
| email | String | Benzersiz, format doğrulaması |
| password | String | BCrypt ile hash'lenmiş |
| role | Enum | `USER`, `ADMIN` |
| createdAt | LocalDateTime | Oluşturma tarihi |

### 4.2 Category (Kategori)
| Alan | Tip | Açıklama |
|---|---|---|
| id | Long | Primary key |
| name | String | Benzersiz kategori adı |
| description | String | Açıklama |

### 4.3 Product (Ürün)
| Alan | Tip | Açıklama |
|---|---|---|
| id | Long | Primary key |
| name | String | Ürün adı |
| description | String | Açıklama |
| price | BigDecimal | Fiyat (para için `double` **kullanma!**) |
| stockQuantity | Integer | Stok adedi |
| category | Category | `@ManyToOne` ilişki |
| createdAt | LocalDateTime | Oluşturma tarihi |
| updatedAt | LocalDateTime | Güncelleme tarihi |

### 4.4 Cart (Sepet)
| Alan | Tip | Açıklama |
|---|---|---|
| id | Long | Primary key |
| user | User | Sepetin sahibi (`@OneToOne` veya `@ManyToOne`) |
| cartItems | List\<CartItem\> | Sepetteki ürünler |

### 4.5 CartItem (Sepet Kalemi)
| Alan | Tip | Açıklama |
|---|---|---|
| id | Long | Primary key |
| cart | Cart | `@ManyToOne` ilişki |
| product | Product | `@ManyToOne` ilişki |
| quantity | Integer | Adet |

### 4.6 Order (Sipariş)
| Alan | Tip | Açıklama |
|---|---|---|
| id | Long | Primary key |
| user | User | Siparişi veren kullanıcı |
| orderItems | List\<OrderItem\> | Sipariş kalemleri |
| totalAmount | BigDecimal | Toplam tutar |
| status | Enum | `PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED` |
| createdAt | LocalDateTime | Sipariş tarihi |

### 4.7 OrderItem (Sipariş Kalemi)
| Alan | Tip | Açıklama |
|---|---|---|
| id | Long | Primary key |
| order | Order | `@ManyToOne` ilişki |
| product | Product | `@ManyToOne` ilişki |
| quantity | Integer | Adet |
| unitPrice | BigDecimal | Sipariş anındaki birim fiyat (snapshot) |

> 💡 **Küçük bir ipucu:** `OrderItem` içindeki `unitPrice`, siparişin verildiği andaki fiyatı saklar. Ürünün fiyatı sonradan değişse bile geçmiş siparişler etkilenmemeli. "Neden ürünün güncel fiyatını kullanmıyoruz?" sorusunu kendine sor — bu tasarım kararının mantığını kavraman, seni iyi bir geliştirici yapacak detaylardan biri.

---

## 5. Geliştireceğin Endpoint'ler

Tüm endpoint'lerin `/api` ile başlamalı, RESTful konvansiyonlara ve doğru HTTP status kodlarına uygun olmalı.

### 5.1 Authentication (Kimlik Doğrulama)

| Method | Endpoint | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/auth/register` | Yeni kullanıcı kaydı | Herkes |
| POST | `/api/auth/login` | Giriş, JWT token döner | Herkes |

Dikkat etmeni istediklerimiz:
- Şifreleri veritabanına **düz metin olarak asla kaydetme**, BCrypt ile hash'le.
- Login başarılı olduğunda bir JWT token dön.
- Token'ın içinde kullanıcı bilgisi (subject) ve rol taşınsın.

### 5.2 Category (Kategori Yönetimi)

| Method | Endpoint | Açıklama | Yetki |
|---|---|---|---|
| GET | `/api/categories` | Tüm kategorileri listeler | Herkes |
| GET | `/api/categories/{id}` | Tek kategori detayı | Herkes |
| POST | `/api/categories` | Kategori oluşturur | ADMIN |
| PUT | `/api/categories/{id}` | Kategori günceller | ADMIN |
| DELETE | `/api/categories/{id}` | Kategori siler | ADMIN |

### 5.3 Product (Ürün Yönetimi)

| Method | Endpoint | Açıklama | Yetki |
|---|---|---|---|
| GET | `/api/products` | Ürünleri listeler (sayfalama + filtre) | Herkes |
| GET | `/api/products/{id}` | Ürün detayı | Herkes |
| POST | `/api/products` | Ürün ekler | ADMIN |
| PUT | `/api/products/{id}` | Ürün günceller | ADMIN |
| DELETE | `/api/products/{id}` | Ürün siler | ADMIN |

Burada seni biraz zorlayacak kısım:
- `GET /api/products` **pagination (sayfalama)** desteklemeli (`page`, `size`, `sort` parametreleri).
- Kategoriye göre filtreleme (`?categoryId=`) ve isme göre arama (`?search=`) ekle.
- Yanıt olarak `Page<ProductResponse>` benzeri bir yapı dön.

### 5.4 Cart (Sepet İşlemleri)

| Method | Endpoint | Açıklama | Yetki |
|---|---|---|---|
| GET | `/api/cart` | Kullanıcının sepetini getirir | USER |
| POST | `/api/cart/items` | Sepete ürün ekler | USER |
| PUT | `/api/cart/items/{itemId}` | Sepetteki ürün adedini günceller | USER |
| DELETE | `/api/cart/items/{itemId}` | Sepetten ürün çıkarır | USER |
| DELETE | `/api/cart` | Sepeti tamamen boşaltır | USER |

Unutma:
- Sepete ürün eklerken stok kontrolü yap (stoktan fazlası eklenememeli).
- Bir kullanıcı yalnızca **kendi** sepetine erişebilmeli.

### 5.5 Order (Sipariş İşlemleri)

| Method | Endpoint | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/orders` | Sepetten sipariş oluşturur (checkout) | USER |
| GET | `/api/orders` | Kullanıcının siparişlerini listeler | USER |
| GET | `/api/orders/{id}` | Sipariş detayı | USER (kendi siparişi) |
| PUT | `/api/orders/{id}/status` | Sipariş durumunu günceller | ADMIN |
| POST | `/api/orders/{id}/cancel` | Siparişi iptal eder | USER |

**Sipariş oluşturma (checkout) — bu projenin en önemli kısmı:**

Bu akışı yazarken bir "an" düşün: kullanıcı "Satın Al" butonuna bastı. O anda arka planda şunların olması gerekiyor:

1. Kullanıcının sepetini al, boşsa hata dön.
2. Her ürün için stok yeterli mi kontrol et.
3. Toplam tutarı hesapla.
4. Siparişi ve sipariş kalemlerini oluştur (fiyatları snapshot olarak kaydet).
5. İlgili ürünlerin stoklarını düş.
6. Sepeti boşalt.
7. Tüm bu adımları **tek bir transaction** içinde yürüt (`@Transactional`). Herhangi bir adım başarısız olursa **hepsi geri alınmalı** (rollback).

> 💡 Bu akış özellikle senin transaction yönetimi ve veri tutarlılığı (consistency) kavramlarını kavraman için tasarlandı. "Stok düştü ama sipariş oluşmadı" gibi bir durumun asla yaşanmaması gerektiğini burada göreceksin.

---

## 6. Kod Kalitesi İçin Beklentilerimiz

### 6.1 DTO Validation
- Tüm request DTO'larını Bean Validation anotasyonlarıyla doğrula: `@NotNull`, `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Positive` vb.
- Controller metodlarında `@Valid` kullan.
- Geçersiz istekler `400 Bad Request` ile anlamlı hata mesajları dönsün.

### 6.2 Exception Handling
- `@RestControllerAdvice` ile **global exception handler** yaz.
- Kendi özel exception sınıflarını oluştur (`ResourceNotFoundException`, `InsufficientStockException` vb.).
- Hata yanıtların tutarlı bir formatta olsun (örn: `timestamp`, `status`, `message`, `path`).

### 6.3 Güvenlik (Security)
- Spring Security + JWT filtresi ile kimlik doğrulama sağla.
- Endpoint'leri role göre yetkilendir (`ADMIN` / `USER`).
- Şifreleri BCrypt ile hash'le.
- Bir kullanıcı başka bir kullanıcının verisine (sepet, sipariş) **erişememeli** — bunu test etmeyi unutma.

### 6.4 API Dokümantasyonu
- Swagger (springdoc-openapi) entegre et.
- Tüm endpoint'lerin `/swagger-ui.html` üzerinden görülebilir ve denenebilir olsun.
- Endpoint'lerini `@Operation`, `@ApiResponse` anotasyonlarıyla açıkla.

### 6.5 Konfigürasyon
- Hassas bilgileri (DB şifresi, JWT secret) kod içine **yazma**; `application.yml` / environment variable üzerinden yönet.

---

## 7. Test Yazımı

Test yazmak bu projenin **en önemli kısımlarından biri** ve senin en çok gelişeceğin alanlardan. "Kodum çalışıyor" ile "kodumun çalıştığını kanıtlayabiliyorum" arasındaki farkı burada göreceksin.

### 7.1 Unit Testler (JUnit 5 + Mockito + AssertJ)
- **Service katmanı** için kapsamlı unit testler yaz.
- Bağımlılıkları (Repository vb.) `@Mock` ile mock'la.
- Assertion'larını **AssertJ** ile yaz (`assertThat(...).isEqualTo(...)`).
- Sadece işlerin yolunda gittiği senaryoları (happy path) değil, hata senaryolarını da test et.

Yazmanı beklediğimiz bazı test senaryoları:
- Stok yetersizse sipariş oluşturulamamalı → exception fırlatılmalı.
- Var olmayan bir ürün istendiğinde `ResourceNotFoundException` fırlatılmalı.
- Sipariş oluşturulduğunda stok doğru miktarda düşmeli.

### 7.2 Integration Testler

- Geliştiridiğin bütün controller'lara uçtan uca test yazmalısın.
- `@SpringBootTest` + `MockMvc` kullanabilirsin. 

---

## 8. Çalışma Şeklin ve Beklentilerimiz

- Kodunu bir **Git reposunda** (GitHub) tut.
- Anlamlı ve düzenli **commit** mesajları yaz — her feature'ı bitirdiğinde commit at.
- Feature branch yapısı ve Pull Request akışını kullanmanı öneririz (kod review pratiği kazanman için birebir).
- Projende bir **README.md** olsun; kurulum adımları, çalıştırma talimatları ve endpoint özeti burada yer alsın.
- Kodun okunabilir, isimlendirmelerin anlamlı ve İngilizce olsun.

---


> İlk hafta biraz yavaş ilerlediğini hissedebilirsin — bu tamamen normal, herkes böyle başlar. İlerledikçe hız kazanacaksın. Bu plan taşa yazılı değil; senin hızına göre birlikte ayarlayabiliriz.

Son bir söz: Bu proje boyunca hata yapman, takılman ve "bunu nasıl yapacağım" diye düşünmen tamamen normal — hatta öğrenmenin ta kendisi bu. Önemli olan denemen, araştırman ve sorman. Biz de bu yolculukta senin yanındayız.

Kolay gelsin, keyifli kodlamalar! 💪
