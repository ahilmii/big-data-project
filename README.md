# School Management — SWE 307

Mevcut Spring Boot 3.5.16 / Java 17 backend’i ve React + TypeScript + Vite frontend’i.
İki ana sayfa vardır: School Management (`/`) ve AI Chat (`/chat`). Yönetim sayfası
Professors, Courses ve Teaching Assignments sekmelerini içerir.

## Başlatma

MySQL Windows üzerinde çalışmalıdır. Windows PowerShell (yönetici) içinde servis
adını `Get-Service *mysql*` ile kontrol edin; örneğin `Start-Service MySQL80`.
WSL gateway adresi değişirse `.env` içindeki `DB_URL` değerini güncelleyin.
MySQL kullanıcısının WSL istemcisinden erişim yetkisi olmalıdır.

WSL içinde, yalnızca çalışmayan servisleri başlatın:

```bash
sudo service redis-server start
sudo -iu hadoop
# Hadoop kullanıcısının mevcut profilinde JAVA_HOME Java 11 olmalıdır.
/usr/local/hadoop/sbin/start-dfs.sh
# Uygulama için HDFS yeterli; ders gösterimi için YARN da başlatılabilir:
/usr/local/hadoop/sbin/start-yarn.sh
exit
```

Backend normal WSL kullanıcısı ve Java 17 ile çalışır:

```bash
# İlk kurulumda .env.example dosyasını .env olarak kopyalayıp gerçek ayarları girin.
# Var olan .env dosyasını üzerine yazmayın.
./scripts/start-backend.sh
```

Script `.env` dosyasındaki Windows CRLF satır sonlarını da destekler. `.env`
Git tarafından yok sayılır. Şifreleri kaynak koda eklemeyin.

Ayrı terminal:

```bash
cd frontend
npm ci
npm run dev
```

Adres: http://127.0.0.1:5173. Vite `/api` isteklerini port 8080’e iletir.
Backend portu değiştirilirse `frontend/vite.config.ts` proxy hedefini de güncelleyin.
Üretim ortamında `/api` reverse proxy ve React Router için SPA fallback gerekir.

Ollama bu geliştirme oturumunun WSL PATH’inde bulunamadı. Önce Ollama’yı
kurun veya Windows kurulumunun WSL’den erişilebilir adresini `OLLAMA_BASE_URL`
ile belirtin. Sonrasında:

```bash
ollama --version
ollama serve
# Başka terminalde:
ollama pull llama3.2:latest
ollama list
```

Model indirmesi bu oturumda yapılmadı. React yalnızca Spring Boot `/api/chat`
endpoint’ini çağırır; backend Ollama `/api/chat` endpoint’ine `stream:false`
ve en fazla 20 geçmiş mesajla gider. Sohbet geçmişi sayfa belleğindedir.

## Özellikler ve endpoint’ler

- `/api/professors`: CRUD; `/{id}/photo`: HDFS fotoğraf yükleme/okuma;
  `/{id}/staged`: Redis DB 2 üzerinden yaklaşık 15 saniyede MySQL’e yazma.
- `/api/courses`: CRUD.
- `/api/assignments`: JOIN listesi ve ekleme; düzenleme/silme kimliği
  `/{professorId}/{courseId}/{startDate}`. Kimlik alanları düzenleme sırasında kilitlidir.
- `/api/chat`: `{ "message": "HDFS nedir?", "history": [] }` gönderir,
  `{ "response": "..." }` döner. History isteğe bağlıdır.
- Formlar, silme onayı, arama, yükleme/boş/hata durumları ve fotoğraf placeholder’ı.
- Profesör düzenleme formunda Redis üzerinden gecikmeli kaydetme seçeneği;
  normal kaydetme doğrudan MySQL’e gider.

## Doğrulama ve sınıf gösterimi

```bash
cd frontend
npm run build
npm run lint
# Repo kökünde; context testi için DB_* ortam değişkenleri gerekir:
set -a
source <(tr -d '\r' < .env)
set +a
./gradlew test
# Dış servislere ihtiyaç duymayan Ollama protokol testleri:
./gradlew test --tests '*ChatServiceTests'
```

Bu oturumda frontend build/lint geçti. Gerçek API testi başlangıçta 5 profesör,
5 ders ve 8 atama döndürdü. Geçici kayıtlarla üç CRUD akışı, JOIN güncellemesi,
ilişkili profesör silmede 409, HDFS PNG yükleme/okuma, Redis cache anahtarı ve
staged verinin MySQL’e aktarılıp pending anahtarının silinmesi doğrulandı.
Geçici MySQL kayıtları temizlendi. Context testi dahil tüm backend testleri geçti.
Test fotoğrafının HDFS temizliği Hadoop kullanıcısına geçiş izni olmadığı için
tamamlanamadı; kalan dosya:
`/professor-images/7-f6f001ee-b5e2-4926-b6ed-f0e543056e40.png`. Cache lookup ölçümleri 15.63 ms ve 10.10 ms
oldu; bu tek örnek bir performans benchmark’ı değildir.

Cache gösterimi için gerçek bir profesörün `GET /api/professors/{id}` isteğini
iki kez çalıştırın; SQL logunu ve DB 1’de `professors::{id}` anahtarını inceleyin.
Gerekirse yalnızca ilgili cache anahtarını `redis-cli -n 1 DEL 'professors::ID'`
ile silin. Pending güncellemeler varken Redis DB 2’yi temizlemeyin.
Staged değişikliği kaydedip 15–30 saniye sonra Yenile düğmesine basın.

Ollama yokken `/api/chat` anlamlı HTTP 503 döndürdü. Gerçek yerel model yanıtı
ve tarayıcı üzerinden görsel/etkileşim testi henüz doğrulanmadı.

## Bilinen sınırlar

Native Hadoop Java Client 3.3.6, HDFS ayarları, Java sürümleri ve veritabanı
şeması korunmuştur. Mevcut fotoğraf servisi MIME tipine güvenir; dosya içeriği
ayrıca doğrulanmıyor. Değiştirilen/silinen fotoğraflar ve başarısız işlemlerin
oluşturabileceği HDFS dosyaları otomatik temizlenmiyor. Redis write-behind
kalıcı bir üretim kuyruğu değildir; eşzamanlı normal/staged güncellemelerin
sıralaması ve silinen profesörün pending kaydı için ek politika gerekebilir.
