# OCR Exam Evaluation Backend

Bu proje, OCR tabanli sinav degerlendirme sisteminin Spring Boot backend katmanidir. Mobil uygulamadan gelen kullanici, sinif, ogrenci ve sinav kagidi isteklerini yonetir; sinav gorselini FastAPI servisine gonderir; donen sonucu veritabanina kaydeder ve mobil uygulamaya geri doner.

Bu README'nin amaci sadece "neler var" demek degil, sistemin neden bu sekilde kuruldugunu ve nasil isledigini adim adim anlatmaktir.

## 1. Sistem Ne Yapiyor?

Bu backend su 4 ana problemi cozer:

1. Ogretmenin sisteme kayit olup giris yapmasi
2. Ogretmenin kendi siniflarini olusturmasi
3. Ogretmenin her sinif icin ogrencilerini tanimlamasi
4. Her ogrenciye ait sinav kagidini yukleyip OCR/NLP sonucunu alip kaydetmesi

Kisaca sistemin gercek hayattaki karsiligi su:

- `User` = genelde ogretmen
- `Group` = ogretmenin sinifi
- `Student` = siniftaki ogrenci
- `ExamSubmission` = yuklenen kagit
- `ExamResult` = kagidin OCR/NLP sonucu

Yani artik sistem "sadece resim yukleyip puan almak" degil, "hangi ogretmenin, hangi sinifta, hangi ogrenci icin, hangi kagidi yukledigi" bilgisini de takip ediyor.

## 2. Yuksek Seviye Mimari

Sistemin genel akisi:

`Mobile App -> Spring Boot -> FastAPI -> OCR + NLP -> Spring Boot -> MySQL -> Mobile App`

Burada Spring Boot'un gorevi:

- mobil uygulamadan gelen HTTP isteklerini almak
- JWT ile kimlik dogrulamak
- kullanicinin kendi verisine erisip erismedigini kontrol etmek
- gorseli FastAPI servisine gondermek
- sonucu alip veritabanina kaydetmek
- istemciye anlamli bir API cevabi donmek

## 3. Katmanli Yapi

Kod tabani temiz sorumluluk ayrimi ile duzenlenmistir:

```text
src/main/java/com/example/demo
├── client/       # FastAPI ile haberlesme
├── config/       # Security, WebClient, property konfigurasyonlari
├── controller/   # REST endpoint'leri
├── dto/          # API request/response modelleri
├── entity/       # Veritabani modelleri
├── exception/    # Global hata yonetimi
├── repository/   # JPA repository katmani
├── security/     # JWT ve Spring Security siniflari
├── service/      # Is kurallari ve servis arayuzleri
└── service/impl/ # Servis implementasyonlari
```

Bu ayrim su yuzden onemli:

- `controller` sadece request/response ile ilgilenir
- `service` is mantigini tasir
- `repository` veritabani sorgularini yapar
- `security` authentication/authorization isini yonetir
- `client` dis servise baglanti isini izole eder

Bu sayede kod buyudugunde her sey tek sinifta toplanmaz.

## 4. Domain Mantigi

### 4.1 User

`User`, sisteme giris yapan hesaptir. Mevcut yapida rol olarak `TEACHER` kullaniliyor.

Alanlar:

- `id`
- `username`
- `email`
- `fullName`
- `password`
- `role`
- `createdAt`

Not:

- `password` plain text tutulmaz, `BCrypt` ile hashlenir.
- `email` her kullanici icin benzersizdir.
- Bir kullanicinin sistemi kullanabilmesi icin once `signup`, sonra `login` yapmasi gerekir.

### 4.2 Group

`Group`, ogretmene ait bir sinifi temsil eder.

Alanlar:

- `id`
- `name`
- `description`
- `teacher`
- `createdAt`

Is kurali:

- Ayni ogretmen kendi icinde ayni isimde iki sinif acamaz.
- Ama iki farkli ogretmenin ayni isimde sinifi olabilir.
- Her group icin en fazla bir adet aktif cevap anahtari kullanilir.

### 4.3 GroupAnswerKey

`GroupAnswerKey`, belirli bir group icin yuklenen referans cevap anahtaridir.

Alanlar:

- `id`
- `group`
- `filePath`
- `originalFileName`
- `versionNumber`
- `active`
- `uploadedAt`

Is kurali:

- Bir group icin ayni anda sadece bir aktif cevap anahtari bulunur.
- Ogretmen yeni cevap anahtari yuklediginde onceki aktif kayit pasif hale gelir.
- Eski paper kayitlari hangi cevap anahtari ile islendiyse onu referans vermeye devam eder.

### 4.4 Student

`Student`, belirli bir sinifa ait ogrencidir.

Alanlar:

- `id`
- `studentNumber`
- `firstName`
- `lastName`
- `group`
- `createdAt`

Is kurali:

- Ayni sinifta ayni `studentNumber` iki kez kullanilamaz.
- Ogrenci dogrudan ogretmene degil, sinifa baglidir.
- Dolayli olarak da sinif uzerinden ogretmene baglidir.

### 4.5 ExamSubmission

`ExamSubmission`, sisteme yuklenen fiziksel kagidin iz kaydidir.

Alanlar:

- `id`
- `uploadedBy`
- `group`
- `student`
- `answerKey`
- `filePath`
- `originalFileName`
- `processedAt`

Bu tablo su soruyu cevaplar:

"Hangi ogretmen, hangi sinif icin, hangi ogrenciye ait hangi dosyayi, hangi cevap anahtari versiyonu ile yukledi ve isledi?"

### 4.6 ExamResult

`ExamResult`, OCR/NLP sonucunun kaydidir.

Alanlar:

- `id`
- `submission`
- `extractedScore`
- `teacherScore`
- `createdAt`

Buradaki mantik su:

- `ExamSubmission` fiziksel yuklemeyi temsil eder
- `ExamResult` bu yuklemenin akademik/degerlendirme sonucunu temsil eder

Bu ayrim bilerek yapildi. Cunku ileride tek submission icin farkli analizler, tekrar isleme, versiyonlama veya audit ihtiyaci olabilir.

## 5. Veritabani Iliskileri

Iliski mantigi su sekildedir:

```text
User (Teacher)
   1 ---- * Group

Group
   1 ---- * Student

Group
   1 ---- * GroupAnswerKey

User (Teacher)
   1 ---- * ExamSubmission

Group
   1 ---- * ExamSubmission

Student
   1 ---- * ExamSubmission

GroupAnswerKey
   1 ---- * ExamSubmission

ExamSubmission
   1 ---- 1 ExamResult
```

Buradaki temel mantik:

- Ogretmen kendi siniflarini olusturur
- Her sinif icin tek bir aktif cevap anahtari tutulur
- Her sinif kendi ogrencilerini tutar
- Yuklenen her kagit bir ogrenciye baglidir
- Kagit ayni zamanda bir sinifa, onu yukleyen ogretmene ve kullanilan cevap anahtarina da baglidir
- Sonuc kaydi ise ilgili kagidin analiz sonucudur

Bu sayede su tip sorular cok rahat cevaplanabilir:

- Bu ogretmenin tum sonuclari neler?
- Bu sinifin kagitlari neler?
- Bu ogrencinin kagit gecmisi nedir?
- Bu sonucun ait oldugu orijinal dosya hangisi?

## 6. Authentication ve Token Mantigi

Bu projede session tabanli giris yok, `JWT` tabanli authentication var.

Yani backend server tarafinda "kullanici oturumu" saklamaz. Bunun yerine her basarili login sonrasinda bir token uretir.

### 6.1 Token nasil uretiliyor?

Login veya signup basarili olunca backend bir JWT uretir.

Token iceriginde su bilgiler bulunur:

- `subject` = username
- `userId`
- `role`
- `issuedAt`
- `expiration`

Bu token `app.jwt.secret` ile imzalanir.

### 6.2 Frontend bu tokeni nasil kullanmali?

Frontendci arkadasina su sekilde anlatabilirsin:

"Login olduktan sonra backend sana bir JWT token donduruyor. Bu token kullanicinin kimligini temsil ediyor. Bundan sonraki korumali butun isteklere `Authorization: Bearer <token>` header'i eklemen gerekiyor. Backend bu token'i okuyup kullanicinin kim oldugunu anliyor."

Yani frontend tarafi her istekte kullanici id gondermez. Kullanici kimligi token'dan cikarilir. Kullanici profil bilgisi ise login response'undan veya `GET /api/auth/user` endpoint'inden alinabilir.

### 6.3 Token akisi

1. Kullanici `signup` veya `login` yapar
2. Backend `AuthResponse` doner
3. Frontend token'i saklar
4. Sonraki tum korumali endpoint'lerde header olarak token'i gonderir
5. `JwtAuthenticationFilter` request'i yakalar
6. Token icinden username okunur
7. Kullanici bulunur
8. Token gecerliyse Spring Security context'ine principal konur
9. Controller tarafinda `@AuthenticationPrincipal` ile aktif kullaniciya erisilir

### 6.4 Frontend icin pratik anlatis

Frontendciye sunu direkt diyebilirsin:

- `signup` ve `login` disindaki tum endpoint'ler token ister
- token'i login cevabindan al
- sonraki butun API isteklerinde:

```http
Authorization: Bearer <token>
```

- backend kullaniciyi bu header uzerinden tanir
- ayri bir `userId` gondermene gerek yok

## 7. Authorization Mantigi

Authentication = "kullanici kim?"

Authorization = "bu kullanici bu veriye erisebilir mi?"

Bu projede authorization mantigi cok onemli. Cunku her ogretmen sadece kendi sinifini, kendi ogrencisini ve kendi kagitlarini gorebilmeli.

Bu yuzden servislerde "sahiplik" kontrolu yapiliyor:

- bir group istenince `teacherId` ile birlikte aranir
- bir student istenince once group ogretmene ait mi kontrol edilir
- sonra ogrenci o group icinde mi kontrol edilir

Bu sayede bir ogretmen baska ogretmenin group veya student id'sini tahmin etse bile veriye erisemez.

## 8. Endpointler

Asagidaki endpointler mevcut mimarinin API yuzudur.

## 8.1 Auth Endpointleri

### `POST /api/auth/signUp`

Amac:

- yeni ogretmen hesabi olusturmak

Request:

```json
{
  "username": "teacher1",
  "password": "12345678",
  "email": "teacher1@example.com",
  "fullName": "Ayse Yilmaz"
}
```

Response mantigi:

- yeni user olusturulur
- sifre hashlenir
- JWT token uretilir
- token ve user bilgisi doner

Ornek response:

```json
{
  "token": "jwt-token",
  "tokenType": "Bearer",
  "expiresInMs": 86400000,
  "user": {
    "id": 1,
    "username": "teacher1",
    "email": "teacher1@example.com",
    "fullName": "Ayse Yilmaz",
    "role": "TEACHER"
  }
}
```

### `POST /api/auth/signIn`

Amac:

- mevcut ogretmenin sisteme giris yapmasi

Request:

```json
{
  "username": "teacher1",
  "password": "12345678"
}
```

Isleyis:

- username ile user bulunur
- girilen sifre ile hashlenmis sifre karsilastirilir
- dogruysa JWT uretilir

### `GET /api/auth/user`

Amac:

- token ile giris yapan mevcut kullanicinin profil bilgisini donmek
- frontend'in uygulama yeniden acildiginda kullanici bilgisini tekrar cekebilmesini saglamak

Header:

```http
Authorization: Bearer <token>
```

Ornek response:

```json
{
  "id": 1,
  "username": "teacher1",
  "email": "teacher1@example.com",
  "fullName": "Ayse Yilmaz",
  "role": "TEACHER"
}
```

## 8.2 Group Endpointleri

Bu endpointlerin tamami token ister.

### `POST /api/classes`

Amac:

- frontend tarafinda yeni bir group olusturmak
- answer key sayfalarini alip tek gorselde birlestirmek
- olusan group listesini guncel haliyle donmek

Request tipi:

- `multipart/form-data`

Alanlar:

- `name`
- `answerKeyPhotos`

Not:

- `answerKeyPhotos` alaninda bir veya daha fazla dosya beklenir
- backend gelen answer key sayfalarini sirasiyla dikey olarak birlestirip tek cevap anahtari gorseli gibi kaydeder
- response `List<ResolvedGroupDTO>` mantiginda group listesi doner

### `GET /api/classes`

Amac:

- login olan ogretmenin tum group kayitlarini listelemek

Donen veri:

- `id`
- `name`
- `examCount`
- `answerKeyImageUrl`
- `exams`

`exams` icindeki her kayit su alanlari doner:

- `fullName`
- `no`
- `score`
- `examImageUrl`

### `PUT /api/classes/{groupId}`

Amac:

- mobil taraftaki group ekranindan tek bir exam kaydini eklemek
- gerekirse `no` alanina gore ogrenciyi bulmak, yoksa olusturmak
- yuklenen kagidi isleyip sade response donmek

Request tipi:

- `multipart/form-data`

Alanlar:

- `postExamDTO`
- `examPhotos`

Not:

- `examPhotos` alaninda bir veya daha fazla dosya beklenir
- backend gelen sayfalari sirasiyla dikey olarak birlestirip mevcut OCR pipeline'ina tek gorsel gibi yollar
- response `fullName`, `no`, `score`, `examImageUrl` alanlarini doner

## 8.3 Asset URL Mantigi

Frontend'in ayrica "answer key get" ya da "exam result detail" endpointlerini bilmesi gerekmez.
JSON response icindeki image URL alanlari yeterlidir:

- `answerKeyImageUrl`
- `examImageUrl`

Bu URL'ler auth korumalidir ve dogrudan image stream doner.
Mobil tarafta image loader (Coil, Glide, Picasso vb.) ile kullanilabilir.

Not:

- bu image endpointleri backend tarafinda vardir
- ancak business API'yi sade tutmak icin Swagger'da gizli tutulabilir

## 9. FastAPI ile Entegrasyon Mantigi

Spring Boot OCR yapmaz.

Spring Boot sadece aracidir:

1. Mobil uygulamadan ogrenci kagidini alir
2. Group icin aktif cevap anahtarini storage'dan bulur
3. FastAPI'ye iki dosyayi multipart olarak yollar
4. FastAPI'den JSON response alir
5. Sonucu kaydeder

Beklenen FastAPI response yapisi:

```json
{
  "questions": [],
  "score": 85
}
```

Bu yapi `FastApiProcessExamResponse` DTO'suna maplenir.

FastAPI ile haberlesme `client/` katmaninda izole edilmiştir. Bunun anlami su:

- yarin FastAPI URL'i degisebilir
- request formati degisebilir
- timeout/retry mantigi eklenebilir

ama bu degisiklikler controller veya service katmanina yayilmaz.

## 10. Frontend Akisi Nasil Olmali?

Mobil uygulama tarafinda tipik kullanim sirasi su olmali:

1. Kullanici `signup` ya da `login` yapar
2. Token alinir
3. Token local secure storage'da tutulur
4. Kullanici `POST /api/classes` ile group olusturur ve answer key sayfalarini birlikte yollar
5. Uygulama `GET /api/classes` ile guncel listeyi ceker
6. Kullanici `PUT /api/classes/{groupId}` ile exam sayfalarini yollar
7. Backend gerekirse ogrenciyi `no` alanina gore otomatik olusturur
8. Group listesi icindeki `examImageUrl` ve `answerKeyImageUrl` alanlari ile gorseller tekrar yuklenir

### Frontend'e anlatilacak sade versiyon

Frontendci arkadasina su sekilde anlat:

"Bu backend teacher tabanli calisiyor. Once login oluyorsun ve token aliyorsun. Sonra `POST /api/classes` ile group'u ve answer key sayfalarini birlikte yolluyorsun. `GET /api/classes` sana hem group listesini hem de mevcut exam kayitlarini donuyor. Yeni kagit eklemek icin `PUT /api/classes/{groupId}` kullaniyorsun. Response icindeki `examImageUrl` ve `answerKeyImageUrl` alanlari da gorselleri tekrar yuklemek icin kullaniliyor."

Bu cok kritik cunku frontend tarafinin anlamasi gereken ana fikir su:

- sinif secimi frontend tarafinda yapilir
- ogrenci secimi frontend tarafinda yapilir
- kullanici kimligi token'dan gelir
- backend tarafinda sahiplik kontrolu yapilir

## 11. Neden Bu Veri Modeli Tercih Edildi?

Bu tasarim keyfi degil, ilerideki ihtiyaclar dusunulerek secildi.

### Neden `User -> Group -> Student` zinciri var?

Cunku gercek dunyada:

- ogretmenin birden fazla sinifi olabilir
- bir sinifin birden fazla ogrencisi olabilir
- kagitlar ogrenciye aittir
- ayni group altindaki tum kagitlar ayni sinav cevap anahtarini kullanabilir

Bu model olmadan "hangi kagit kime ait?" sorusu guvenilir sekilde cevaplanamaz.

### Neden `ExamSubmission` ve `ExamResult` ayri?

Cunku bunlar ayni sey degil:

- `ExamSubmission` = dosya yukleme olayi
- `ExamResult` = analiz sonucu

Bu ayrim ileride su senaryolari kolaylastirir:

- ayni dosyayi tekrar islemek
- farkli analiz versiyonlari tutmak
- dosya ile sonuc kaydini ayri denetlemek

### Neden `GroupAnswerKey` ayri bir tablo?

Cunku cevap anahtari group'un basit bir string alani olmaktan daha fazlasidir:

- versiyon bilgisi tutulur
- hangi kagidin hangi cevap anahtariyla islendigini kaybetmeyiz
- yeni cevap anahtari yuklenince eski submission kayitlari bozulmaz
- ileride tekrar isleme veya audit ihtiyaclari kolaylasir

### Neden token icine `userId` ve `role` koyduk?

Cunku request geldiginde veritabani disinda da hizli kimlik bilgisi tasinmis olur.

Ama yine de guvenlik olarak asil kontrol sadece token payload ile degil, veritabani ve service katmanindaki sahiplik kontrolleri ile birlikte yapilir.

## 12. Guvenlik Kurallari

Mevcut yapida temel guvenlik kurallari:

- `/api/auth/**` acik
- diger tum endpoint'ler korumali
- sifreler hashli saklanir
- kullanici kimligi JWT ile tasinir
- ogretmen sadece kendi verisine erisebilir

Bu sistem "frontend userId gondersin, backend ona guvensin" mantiginda degildir.
Asil kimlik kaynagi JWT token'dir.

## 13. Hata Durumlari

Global exception handling ile anlamli HTTP status donulmesi hedeflenmistir.

Ornek durumlar:

- `400 Bad Request` -> validation hatasi / eksik parametre
- `401 Unauthorized` -> token yok veya gecersiz
- `403 Forbidden` -> erisim izni yok
- `404 Not Found` -> group/student/user bulunamadi
- `409 Conflict` -> ayni username veya ayni group/student number tekrar deneniyor
- `502 Bad Gateway` -> FastAPI tarafinda hata

Frontend acisindan bunun anlami:

- her hatayi tek bir generic "bir sey bozuldu" mesaji gibi gostermek yerine
- status code'a gore kullaniciya daha anlamli mesaj vermek mumkun

## 14. Uygulamadaki Ana Is Kurallari

Sistemde dikkat edilmesi gereken ana kurallar:

1. Her group bir ogretmene aittir
2. Her group icin ayni anda sadece bir aktif cevap anahtari bulunur
3. Her student bir group'a aittir
4. Bir kagit yuklenirken hem group hem student belirtilmelidir
5. Kagit islenmeden once ilgili group icin aktif cevap anahtari bulunmak zorundadir
6. Student, verilen group icinde olmak zorundadir
7. Login olan ogretmen, sadece kendi group ve student verileriyle islem yapabilir
8. Her submission tek bir exam result ile eslenir
9. Her submission kullanilan cevap anahtari versiyonunu referanslar
10. Password hashlenmeden kaydedilmez

## 15. Projenin Ozeti

Bu backend'in mantigi tek cumlede su:

"Ogretmen once kimligini dogrular, sonra kendi sinifini ve o sinifin cevap anahtarini tanimlar, daha sonra her ogrenciye ait kagidi yukler; backend aktif cevap anahtari ile bu kagidi FastAPI'de isler ve sonucu ilgili ogretmen-sinif-ogrenci baglaminda kalici olarak kaydeder."

Eger frontend ekibi bu mantigi anlarsa entegrasyon cok rahat ilerler:

- auth token'i dogru yonet
- class sec
- answer key yukle
- student sec
- paper yukle
- sonucu listele

Bu sistemin omurgasi budur.
