# Spring Boot ile AWS S3 Dosya Yükleyici

## Proje Genel Bakış

Bu proje, Spring Boot kullanarak AWS S3'e dosya yükleme, indirme ve silme işlemlerini nasıl gerçekleştirebileceğinizi gösteren bir örnek uygulamadır. Uygulama, dosya doğrulama, yükleme, indirme ve silme işlemleri için AWS S3 entegrasyonu sağlar. Ayrıca, dosya meta verilerini veritabanında saklar ve takip eder.

## Özellikler

- **Dosya Yükleme**: JPEG, PNG, GIF formatlarında 5MB'a kadar olan dosyaları AWS S3'e yükleyebilirsiniz.
- **Dosya İndirme**: AWS S3'ten dosya indirebilirsiniz.
- **Dosya Silme**: AWS S3'ten dosya silme ve veritabanındaki dosya meta verisini kaldırma işlemleri yapılabilir.
- **Dosya Meta Verisi**: Dosya adı ve URL'si gibi meta veriler Spring Data JPA kullanılarak veritabanında saklanır.
- **Dosya Doğrulama**: Yüklenen dosyanın boyutu ve içerik türü doğrulanır.

## Kullanılan Teknolojiler

- **Spring Boot**: REST API oluşturmak için kullanılan framework.
- **AWS S3**: Yüklenen dosyaların depolanacağı bulut hizmeti.
- **Spring Data JPA**: Dosya meta verilerini yönetmek için kullanılan veri erişim katmanı.
- **SLF4J**: Uygulama işlemlerini izlemek için kullanılan loglama framework'ü.
- **AWS SDK v2**: AWS servisleriyle etkileşim için kullanılan AWS SDK.

## Kurulum Talimatları

### Gereksinimler

1. **AWS Hesabı**: AWS S3 servislerine erişim için bir AWS hesabınız olmalıdır.
2. **AWS Erişim Anahtarları**: AWS S3'e bağlanmak için erişim anahtarlarınızı elde edin.
3. **Spring Boot Projesi**: Spring Boot ortamının kurulmuş olması gerekmektedir.
4. **Uygulamayı Başlatın:** Spring Boot uygulamanızı başlatın ve /upload, /download/{fileName}, /delete/{fileId} gibi API uç noktalarını kullanarak dosya işlemlerini gerçekleştirebilirsiniz.

### Adımlar

1. **AWS S3 Bucket Oluşturma**: AWS S3'te bir bucket oluşturun ve bu bucket'ın adını `application.properties` dosyasına ekleyin.
2. **Gerekli Bağımlılıkları Ekleyin:** Projeye AWS SDK ve Spring Data JPA gibi gerekli bağımlılıkları ekleyin.
3. **Veritabanı Yapılandırması:** Dosya meta verilerini saklamak için Spring Data JPA ile bağlantı sağlayın.


Katkıda Bulunma
Katkıda bulunmak isterseniz, projenize pull request (PR) açarak önerilerde bulunabilirsiniz. Yeni özellikler eklemek veya hataları düzeltmek için katkılarınızı bekliyoruz.
