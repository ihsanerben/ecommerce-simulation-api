# JWT Güvenlik Mimarisi ve Uçtan Uca Authentication Akışları

Bu doküman, projede uygulanan yeni authentication mimarisindeki kavramları ve register, login, JWT doğrulama, refresh, logout ve şifre sıfırlama akışlarını uçtan uca açıklar.

## 1. Temel güvenlik kavramları

### Authentication

Authentication, “Bu isteği yapan kişi kim?” sorusunun cevabıdır. Login sırasında kullanıcı adı ve şifre doğrulanır. Başarılı olduğunda backend kullanıcının kimliğini temsil eden access ve refresh token üretir.

### Authorization

Authorization, kimliği doğrulanan kullanıcının ne yapabileceğini belirler.

```text
Authentication → Sen kimsin?
Authorization  → Bu işlemi yapmaya yetkin var mı?
```

Örneğin `USER` kendi sepetini kullanabilirken ürün oluşturmak için `ADMIN` rolü gerekir. Projede rol kontrolleri `@PreAuthorize("hasRole('ADMIN')")` ile yapılır.

### Session

Session, login ile başlayan ve logout, süre sonu veya güvenlik nedeniyle iptal edilene kadar devam eden oturumdur. Yeni sistemde her login için bir `AuthSession` kaydı oluşturulur:

```text
AuthSession
├── user
├── refreshTokenHash
├── familyId
├── expiresAt
├── revokedAt
├── createdAt
├── lastUsedAt
├── userAgent
└── ipAddress
```

Bir kullanıcı telefon ve bilgisayardan login olursa iki ayrı session ve token ailesi oluşur. Normal logout mevcut session'ı, `logout-all` bütün session'ları kapatır.

### JWT

JWT, kullanıcı kimliğiyle ilgili bilgileri imzalı biçimde taşıyan token formatıdır:

```text
header.payload.signature
```

- Header kullanılan imza algoritmasını belirtir.
- Payload claim adı verilen bilgileri taşır.
- Signature, token'ın backend tarafından üretildiğini ve değiştirilmediğini doğrular.

JWT payload şifrelenmiş değildir; Base64 ile kodlanır ve okunabilir. Bu yüzden JWT içine şifre, kredi kartı veya gizli anahtar gibi hassas bilgiler konulmaz. Güvenlik payload'ın gizli olmasından değil, imzanın sahte üretilememesinden gelir.

### JWT claim'leri

Access token örneği:

```json
{
  "sub": "ihsan",
  "userId": 42,
  "role": "USER",
  "type": "access",
  "tokenVersion": 0,
  "jti": "1d79b207-...",
  "iat": 1784320000,
  "exp": 1784320900
}
```

- `sub`: Token sahibinin kullanıcı adı.
- `userId`: Kullanıcının veritabanı kimliği.
- `role`: Kullanıcının rolü.
- `type`: Token'ın `access` veya `refresh` olduğunu belirtir.
- `jti`: Token'ın benzersiz kimliğidir; blacklist kaydında kullanılır.
- `iat`: Token'ın oluşturulma zamanıdır.
- `exp`: Token'ın sona erme zamanıdır.
- `tokenVersion`: Kullanıcının token sürümüdür. Şifre değişimi veya `logout-all` sonrasında artırılarak eski token'lar topluca geçersiz kılınır.
- `familyId`: Sadece refresh token'da bulunur ve rotation zincirinin ait olduğu oturumu tanımlar.

### Access token

Access token normal API isteklerinde kimlik doğrulamak için kullanılır.

```http
GET /api/cart
Cookie: access_token=eyJ...
```

Özellikleri:

- Varsayılan ömrü 15 dakikadır.
- `type=access` taşır.
- Benzersiz `jti` değeri vardır.
- HttpOnly cookie'de saklanır.
- Logout sırasında blacklist'e eklenerek süresi bitmeden geçersiz kılınabilir.

Kısa ömür, çalınan token'ın kullanılabileceği süreyi azaltır.

### Refresh token

Refresh token, access token sona erdiğinde kullanıcıdan yeniden şifre istemeden yeni token çifti üretmek için kullanılır.

```http
POST /api/auth/refresh
Cookie: refresh_token=eyJ...
```

Özellikleri:

- Varsayılan ömrü 7 gündür.
- `type=refresh` taşır.
- Normal API erişiminde kullanılamaz.
- Server-side `AuthSession` kaydıyla doğrulanır.
- Her kullanımda rotate edilir.
- Veritabanında token'ın kendisi değil SHA-256 hash'i saklanır.

### Token expiration

Varsayılan süreler:

```text
JWT_ACCESS_EXPIRATION_MS=900000       # 15 dakika
JWT_REFRESH_EXPIRATION_MS=604800000   # 7 gün
```

`exp` zamanı geçen JWT'nin imzası doğru olsa bile token kabul edilmez.

### Token rotation

Refresh token her kullanıldığında eski token iptal edilir ve yenisi oluşturulur:

```text
Login → R1
R1 ile refresh → R1 revoked, R2 üretildi
R2 ile refresh → R2 revoked, R3 üretildi
```

Bu mekanizma aynı refresh token'ın uzun süre tekrar tekrar kullanılmasını engeller.

### Refresh token reuse

Daha önce kullanılmış veya iptal edilmiş refresh token'ın yeniden gönderilmesine reuse denir.

```text
Gerçek kullanıcı R1 ile refresh yaptı → R2 üretildi
Saldırgan çaldığı R1'i yeniden gönderdi
```

Backend R1'in `revokedAt` alanının dolu olduğunu görür. Token'ın kopyalanmış olabileceğini varsayarak aynı `familyId` altındaki bütün aktif token'ları iptal eder. R2 de geçersiz olur ve kullanıcı yeniden login olmak zorunda kalır.

### Token family

Bir session boyunca rotation ile üretilen refresh token'lar aynı aileye aittir:

```text
Bilgisayar / Family F1
├── R1 revoked
├── R2 revoked
└── R3 active

Telefon / Family F2
└── R1 active
```

F1 ailesinde reuse tespit edilmesi F2 oturumunu etkilemez.

### Pessimistic lock

Aynı refresh token'ın eşzamanlı iki istek tarafından kullanılmasını engeller. İlk refresh isteği session satırını kilitler. İkinci istek bekler ve kilit açıldığında token'ın artık revoked olduğunu görür. Böylece aynı refresh token'dan iki ayrı geçerli token üretilemez.

### Token hash ve SHA-256

Refresh ve reset token'ların ham değerleri veritabanına yazılmaz:

```text
raw token → SHA-256 → token hash → veritabanı
```

İstek geldiğinde token tekrar hash'lenip kayıt aranır. Veritabanı sızsa bile hash değerleri doğrudan refresh veya reset token olarak kullanılamaz.

### BCrypt

Kullanıcı şifreleri BCrypt ile hash'lenir. Aynı şifre her hash işleminde farklı sonuç üretebilir. Bu nedenle iki hash string olarak karşılaştırılmaz:

```java
passwordEncoder.matches(rawPassword, storedHash)
```

ile doğrulanır.

### Cookie

Cookie, tarayıcının belirli bir domain için saklayıp uygun isteklere otomatik eklediği küçük veridir. Login response'unda backend `Set-Cookie` header'ları gönderir; sonraki isteklerde tarayıcı bunları `Cookie` header'ıyla taşır.

### HttpOnly cookie

HttpOnly cookie JavaScript tarafından `document.cookie` ile okunamaz. Bu özellik, XSS durumunda token'ın doğrudan okunup saldırgana gönderilmesini zorlaştırır. Tarayıcı cookie'yi uygun isteklere yine otomatik ekler.

### XSS

Cross-Site Scripting, saldırganın uygulama içinde JavaScript çalıştırabilmesidir. Token `localStorage` içinde olsaydı zararlı JavaScript token'ı okuyabilirdi. HttpOnly cookie bu riski azaltır; ancak XSS'yi tek başına tamamen çözmez. Output escaping, input kontrolü ve Content Security Policy gibi savunmalar yine gereklidir.

### Secure cookie

`Secure` cookie yalnızca HTTPS üzerinden gönderilir. Production ortamında zorunlu olarak açılmalıdır:

```text
AUTH_COOKIE_SECURE=true
```

Localhost geliştirmesinde HTTPS olmadığı için `false` kullanılabilir.

### SameSite

SameSite, cookie'nin cross-site isteklerde gönderilmesini sınırlar:

- `Strict`: En sıkı seçenektir; cross-site kullanım büyük ölçüde engellenir.
- `Lax`: Güvenlik ve kullanım kolaylığı arasında dengelidir; mevcut varsayılandır.
- `None`: Cross-site cookie'ye izin verir, ancak `Secure=true` zorunludur.

Frontend ve backend deployment yapısına göre değer seçilmelidir.

### Cookie Path

Cookie'nin hangi URL yollarına gönderileceğini belirler:

```text
access_token  → Path=/
refresh_token → Path=/api/auth
```

Refresh token bu sayede ürün, sepet ve sipariş endpoint'lerine gereksiz yere gönderilmez.

### Cookie Domain

Cookie'nin hangi host veya subdomain'lerde kullanılacağını belirler. Boş bırakıldığında host-only cookie oluşur. Birden fazla subdomain arasında paylaşım gerekiyorsa production domain'i dikkatli biçimde ayarlanmalıdır.

### Cookie temizleme

Logout sırasında backend aynı isim ve path ile `Max-Age=0` cookie gönderir:

```http
Set-Cookie: access_token=; Max-Age=0; Path=/
Set-Cookie: refresh_token=; Max-Age=0; Path=/api/auth
```

Cookie temizliği tek başına güvenli logout değildir. Kopyalanmış token'lar için server-side invalidation da gerekir.

### CSRF

Cross-Site Request Forgery, saldırganın başka bir siteden kullanıcının tarayıcısına authenticated istek yaptırmasıdır. Cookie otomatik gönderildiği için saldırgan token değerini bilmese bile kullanıcının session'ından faydalanmaya çalışabilir.

### CSRF token

Frontend önce şu endpoint'i çağırır:

```http
GET /api/auth/csrf
```

Backend JavaScript tarafından okunabilen `XSRF-TOKEN` cookie'si üretir. Frontend state-changing isteklerde bu değeri header'a koyar:

```http
Cookie: XSRF-TOKEN=abc123
X-XSRF-TOKEN: abc123
```

Başka origin'deki saldırgan site cookie değerini okuyamadığı için doğru header'ı oluşturamaz. JWT cookie'leri HttpOnly'dir; CSRF cookie'sinin okunabilir olması ise tasarım gereğidir.

### State-changing request

Sunucudaki veriyi değiştiren `POST`, `PUT`, `PATCH` ve `DELETE` istekleridir. CSRF koruması özellikle bu istekler için önemlidir. REST tasarımında GET istekleri veri değiştirmemelidir.

### CORS

Origin `scheme + host + port` birleşimidir. `http://localhost:3000` ve `http://localhost:8080` farklı origin'dir. Backend yalnızca izin verilen frontend origin'inden gelen browser isteklerini kabul eder:

```text
FRONTEND_ORIGIN=http://localhost:3000
```

Cookie kullanıldığı için frontend credentials göndermelidir:

```javascript
fetch(url, { credentials: "include" });
```

veya:

```javascript
axios.defaults.withCredentials = true;
```

### Preflight request

Tarayıcı bazı cross-origin isteklerden önce `OPTIONS` isteği göndererek method ve header'ların izinli olup olmadığını kontrol eder. Backend `Content-Type`, `X-XSRF-TOKEN` ve gerektiğinde `Authorization` header'larına izin verir.

### Token blacklist

JWT normalde süresi dolana kadar geçerlidir. Logout sırasında access token'ın `jti` değeri ve expiration zamanı `revoked_access_tokens` tablosuna yazılır. Her authenticated istekte `jti` blacklist'te aranır. Blacklist'teyse imza ve süre doğru olsa bile token reddedilir.

### Server-side invalidation

Token'ın yalnızca browser'dan silinmesi değil, sunucu tarafında da kullanılamaz hale getirilmesidir:

```text
Refresh token → AuthSession.revokedAt = now
Access token  → jti blacklist'e eklenir
```

### Stateless, stateful ve hibrit yapı

Saf JWT stateless olabilir; sunucu sadece imza ve expiration kontrol eder. Bu proje hibrit yapı kullanır:

```text
Access token   → JWT tabanlı
Refresh token  → Server-side session ile takipli
Logout         → Blacklist ve session invalidation ile stateful
```

### `revokedAt`, `expiresAt` ve `lastUsedAt`

- `revokedAt`: Session'ın ne zaman iptal edildiğini gösterir. `null` ise henüz revoke edilmemiştir.
- `expiresAt`: Session'ın doğal sona erme zamanıdır.
- `lastUsedAt`: Refresh token'ın en son ne zaman kullanıldığını gösterir.

Session yalnızca `revokedAt == null` ve `expiresAt > now` ise aktiftir.

### User-Agent ve IP adresi

Session kaydında tarayıcı bilgisi ve IP tutulur. Bunlar ileride cihaz listesi ve güvenlik denetimi için kullanılabilir; ancak client tarafından değiştirilebildikleri veya doğal olarak değişebildikleri için tek başlarına kimlik doğrulama kanıtı değildir.

### Logout ve logout-all

Normal logout mevcut session'ı kapatır, mevcut access token'ı blacklist'e alır ve cookie'leri temizler. `logout-all`, kullanıcıya ait bütün refresh session'ları revoke eder ve `tokenVersion` artırarak diğer cihazlardaki access token'ları da geçersiz kılar.

### Password reset token

Şifre sıfırlama token'ı 32 byte `SecureRandom` ile üretilen, URL-safe Base64 biçiminde, 15 dakika geçerli ve tek kullanımlık opaque token'dır. Veritabanında yalnızca SHA-256 hash'i tutulur.

### Opaque token

Client açısından iç yapısı anlam taşımayan rastgele token'dır. Password reset token'ın claim taşımasına ihtiyaç olmadığı için JWT yerine opaque token kullanılır.

### Account enumeration

Saldırganın hangi e-postaların sistemde kayıtlı olduğunu öğrenmesidir. Forgot-password endpoint'i e-posta kayıtlı olsun veya olmasın aynı cevabı döndürür:

```json
{
  "message": "If an account exists for this email, a reset link has been sent."
}
```

### Password history

Kullanıcının son üç BCrypt şifre hash'i tutulur. Yeni şifre her eski hash'e `passwordEncoder.matches` ile karşılaştırılır. Eşleşirse `409 Conflict` döner. Mevcut şifre ve önceki iki şifre, “son üç kullanılan şifre” olarak değerlendirilir.

### Transaction

Birden fazla veritabanı işlemini atomik bütün olarak yürütür. Şifre sıfırlamada şifrenin güncellenmesi, geçmişe eklenmesi, session'ların iptali ve reset token'ın tüketilmesi aynı transaction içindedir. Bir işlem başarısız olursa tamamı rollback edilir.

### Cookie-first authentication ve Bearer uyumluluğu

JWT filtresi önce `access_token` cookie'sine bakar. Cookie yoksa testler, Swagger ve API araçları için `Authorization: Bearer ...` header desteği devam eder. Normal browser frontend cookie yöntemini kullanmalı ve token'ı JavaScript'te saklamamalıdır.

## 2. Uçtan uca akışlar

### Register akışı

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "ihsan",
  "email": "ihsan@example.com",
  "password": "password123"
}
```

1. Username, e-posta ve şifre Bean Validation ile doğrulanır.
2. Username ve e-postanın benzersiz olduğu kontrol edilir.
3. Şifre BCrypt ile hash'lenir; ham şifre saklanmaz.
4. Kullanıcı `USER` rolü ve `tokenVersion=0` ile kaydedilir.
5. İlk şifre hash'i password history'ye eklenir.
6. Yeni session ve `familyId` oluşturulur.
7. 15 dakikalık access token üretilir.
8. 7 günlük refresh token üretilir.
9. Refresh token'ın SHA-256 hash'i `AuthSession` kaydına yazılır.
10. Access ve refresh token HttpOnly cookie olarak response'a eklenir.
11. Body yalnızca kullanıcı adı ve rolü içerir.

```json
{
  "username": "ihsan",
  "role": "USER"
}
```

Register aynı zamanda otomatik login oluşturur; token body'de bulunmaz.

### CSRF başlangıç akışı

Frontend state-changing isteklerden önce:

```http
GET /api/auth/csrf
```

çağırır. Dönen `XSRF-TOKEN` cookie değeri sonraki isteklerde `X-XSRF-TOKEN` header'ına konur. Test profilinde eski MockMvc sözleşmelerini korumak için CSRF kapalıdır; normal uygulamada varsayılan olarak açıktır.

### Login akışı

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "ihsan",
  "password": "password123"
}
```

1. Request validation çalışır.
2. Spring `AuthenticationManager` kullanıcıyı yükler.
3. Ham şifre BCrypt ile veritabanındaki hash'e karşı doğrulanır.
4. Yanlış bilgilerde `401 Unauthorized` ve genel bir hata mesajı dönülür.
5. Başarılıysa her login için yeni server-side session oluşturulur.
6. Access ve refresh token üretilir.
7. Refresh token hash'i session'a yazılır.
8. Token'lar HttpOnly cookie olarak gönderilir.
9. Body yalnızca username ve role içerir.

Aynı kullanıcının farklı cihazlardaki login'leri ayrı session ve family oluşturur.

### Authenticated API isteği

```http
GET /api/cart
Cookie: access_token=eyJ...
```

JWT filtresi sırasıyla:

1. Access cookie'yi, yoksa Bearer header'ı okur.
2. JWT imzasını ve expiration'ı doğrular.
3. Username ve `jti` claim'lerini çıkarır.
4. `jti` blacklist'te mi kontrol eder.
5. Kullanıcıyı veritabanından yükler.
6. Token tipinin `access` olduğunu doğrular.
7. Username ve `tokenVersion` değerlerini karşılaştırır.
8. Başarılıysa Spring Security context'e authenticated `UserPrincipal` yerleştirir.

Controller `@AuthenticationPrincipal` ile kullanıcı ID'sini alır ve servis yalnızca o kullanıcıya ait kayıtlara erişir.

### Access token sona erdiğinde refresh

Frontend access token kaynaklı authentication hatası aldığında bir kez:

```http
POST /api/auth/refresh
Cookie: refresh_token=R1
```

çağırır. Backend:

1. Refresh cookie'nin varlığını kontrol eder.
2. JWT imzası, expiration ve `type=refresh` kontrolü yapar.
3. Token hash'iyle session kaydını pessimistic lock ile yükler.
4. Session'ın aktif ve token family'nin doğru olduğunu doğrular.
5. R1 session'ını revoke eder ve `lastUsedAt` yazar.
6. Aynı family içinde yeni access token A2 ve refresh token R2 üretir.
7. R2 hash'i için yeni session kaydı oluşturur.
8. Yeni token'ları cookie olarak gönderir.

Refresh başarılıysa frontend başarısız olan ilk API isteğini tekrarlar. Refresh başarısızsa kullanıcı login ekranına yönlendirilir.

### Eski refresh token reuse

R1 kullanılarak R2 üretildikten sonra R1 tekrar gönderilirse backend R1 session'ının revoked olduğunu görür. Bu olası token hırsızlığıdır. Aynı family'deki R2 dahil bütün aktif token'lar revoke edilir ve `401 Unauthorized` döner. Kullanıcı yeniden login olmalıdır.

### Normal logout

```http
POST /api/auth/logout
Cookie: access_token=A1
Cookie: refresh_token=R1
X-XSRF-TOKEN: ...
```

1. R1 hash'iyle session bulunur ve `revokedAt=now` yapılır.
2. A1 içindeki `jti` expiration zamanı ile blacklist'e eklenir.
3. Access ve refresh cookie'ler `Max-Age=0` ile temizlenir.
4. `204 No Content` döner.

Logout sonrasında kopyalanmış A1 blacklist nedeniyle, R1 ise session revoked olduğu için kullanılamaz. Logout idempotent'tir; tekrar çağrılması sistemi bozmaz.

### Logout-all

```http
POST /api/auth/logout-all
```

1. Kullanıcının bütün aktif refresh session'ları revoke edilir.
2. `User.tokenVersion` artırılır.
3. Mevcut access token blacklist'e eklenir.
4. Mevcut browser cookie'leri temizlenir.

Diğer cihazlardaki refresh token'lar revoked, access token'lar ise eski token version taşıdıkları için geçersiz olur.

### Forgot-password

```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "ihsan@example.com"
}
```

E-posta kayıtlıysa:

1. Kullanıcı case-insensitive e-postayla bulunur.
2. Eski reset token kayıtları temizlenir.
3. `SecureRandom` ile 32 byte token üretilir.
4. Token URL-safe Base64'e çevrilir.
5. SHA-256 hash'i veritabanına, 15 dakikalık expiration ile yazılır.
6. Raw token içeren frontend linki Gmail SMTP üzerinden gönderilir.

```text
https://frontend.example/reset-password?token=raw-token
```

E-posta kayıtlı değilse işlem yapılmaz. Her iki durumda da account enumeration'ı engelleyen aynı genel response döner.

### Gmail gönderimi

Gerekli environment değerleri:

```text
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=google-app-password
```

Normal Google hesap şifresi değil, Google App Password kullanılmalıdır. SMTP authentication ve STARTTLS aktiftir.

### Reset-password

```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "emaildeki-raw-token",
  "newPassword": "new-password-123"
}
```

1. Token ve yeni şifre validation'dan geçer.
2. Raw token SHA-256 ile hash'lenir ve reset kaydı bulunur.
3. Token'ın kullanılmamış ve süresinin geçmemiş olduğu kontrol edilir.
4. Yeni şifre son üç BCrypt hash'e `matches` ile karşılaştırılır.
5. Tekrar kullanılan şifrede `409 Conflict` dönülür.
6. Yeni şifre BCrypt ile hash'lenip kullanıcıya yazılır.
7. `tokenVersion` artırılarak eski access token'lar geçersiz kılınır.
8. Yeni hash password history'ye eklenir ve yalnızca son üç kayıt tutulur.
9. Bütün aktif refresh session'lar revoke edilir.
10. Reset token'a `usedAt=now` yazılarak ikinci kullanım engellenir.
11. Kullanıcının yeni şifresiyle yeniden login olması istenir.

Bu işlemlerin tamamı tek transaction içinde yürür.

## 3. Frontend yaşam döngüsü

```text
Uygulama açıldı
      ↓
GET /api/auth/csrf
      ↓
XSRF-TOKEN alındı
      ↓
Register veya login
      ↓
Backend HttpOnly access + refresh cookie yazdı
      ↓
Normal API istekleri cookie ile çalışır
      ↓
Access token sona erdi
      ↓
POST /api/auth/refresh
      ↓
Yeni token cookie'leri yazıldı
      ↓
Başarısız ilk istek tekrarlandı
      ↓
Logout
      ↓
Session revoke + blacklist + cookie temizliği
```

Frontend örneği:

```javascript
await fetch("http://localhost:8080/api/auth/login", {
  method: "POST",
  credentials: "include",
  headers: {
    "Content-Type": "application/json",
    "X-XSRF-TOKEN": csrfToken
  },
  body: JSON.stringify({
    username: "ihsan",
    password: "password123"
  })
});
```

Frontend JWT'yi `localStorage` veya JavaScript state içinde saklamaz ve normal browser akışında Authorization header üretmez. Token yönetimini browser cookie mekanizması ve backend gerçekleştirir.

## 4. Endpoint özeti

| Method | Endpoint | Amaç |
|---|---|---|
| GET | `/api/auth/csrf` | CSRF cookie'sini başlatır |
| POST | `/api/auth/register` | Kullanıcı ve güvenli session oluşturur |
| POST | `/api/auth/login` | Kimlik doğrular ve session oluşturur |
| POST | `/api/auth/refresh` | Refresh rotation ile token çiftini yeniler |
| POST | `/api/auth/logout` | Mevcut session ve token'ları iptal eder |
| POST | `/api/auth/logout-all` | Kullanıcının bütün session'larını iptal eder |
| POST | `/api/auth/forgot-password` | Şifre sıfırlama e-postası gönderir |
| POST | `/api/auth/reset-password` | Şifreyi güvenli biçimde değiştirir |

## 5. Güvenlik mekanizmaları özeti

| Mekanizma | Çözdüğü problem |
|---|---|
| HttpOnly cookie | JavaScript'in token'ı doğrudan okumasını engeller |
| Secure cookie | Token'ın HTTP üzerinden gönderilmesini engeller |
| SameSite | Cross-site cookie kullanımını sınırlar |
| CSRF token | Başka sitelerin kullanıcı adına işlem yapmasını engeller |
| Kısa access token | Çalınan access token'ın kullanım süresini azaltır |
| Refresh token | Kullanıcıyı sürekli login olmaktan kurtarır |
| Refresh rotation | Aynı refresh token'ın tekrar kullanılmasını engeller |
| Token family | Reuse durumunda ilgili oturumu topluca kapatır |
| Token hash | Veritabanı sızıntısında raw token'ları korur |
| Pessimistic lock | Eşzamanlı refresh yarışını engeller |
| Access blacklist | Logout sonrası access token'ı hemen geçersiz kılar |
| Server-side session | Refresh token üzerinde merkezi iptal kontrolü sağlar |
| Token version | Bütün access token'ları topluca geçersiz kılar |
| Password history | Son üç şifrenin tekrar kullanılmasını engeller |
| Tek kullanımlık reset token | Reset linkinin tekrar kullanılmasını engeller |
| Reset expiration | Çalınmış reset linkinin kullanım süresini sınırlar |
| Account enumeration koruması | Kayıtlı e-postaların keşfedilmesini zorlaştırır |
| Session invalidation | Şifre değişiminden sonra eski oturumları kapatır |

## 6. Production ayarları

En azından aşağıdaki değerler production ortamında açıkça ayarlanmalıdır:

```text
JWT_SECRET=<güçlü-ve-uzun-secret>
JWT_ACCESS_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000
AUTH_COOKIE_SECURE=true
AUTH_COOKIE_SAME_SITE=Lax
AUTH_COOKIE_DOMAIN=
FRONTEND_ORIGIN=https://frontend.example.com
FRONTEND_RESET_PASSWORD_URL=https://frontend.example.com/reset-password
CSRF_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<gmail-adresi>
MAIL_PASSWORD=<google-app-password>
```

Frontend ve backend farklı site bağlamlarında çalışıyorsa `SameSite=None` ve `Secure=true` gerekebilir. Bu karar gerçek deployment domain yapısına göre verilmelidir.
