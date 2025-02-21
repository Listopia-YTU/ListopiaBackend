# Contributing Guidelines

Teşekkürler! Bu projeye katkıda bulunmak istiyorsanız, lütfen aşağıdaki kurallara uyduğunuzdan emin olun. Bu kurallar, projenin düzenli ve sürdürülebilir bir şekilde gelişmesini sağlamak için belirlenmiştir.

## 🚀 Branch Kullanımı

Tüm geliştiriciler aşağıdaki branch kurallarına uymalıdır:

- **Ana branch'ler:**
    - `main`: Sadece production için.
    - `development`: Geliştirme branch'i, tüm yeni özellikler buraya merge edilir.
    - `relase`: Yayınlama branch'ı, sunucuya gidip çalışacak olan sürüm için.

- **Özelleştirilmiş branch'ler:**
    - Yeni bir geliştirme yaparken `dev/{isim}` formatını kullanın.
    - Örnek: `dev/ahmet`, `dev/ayse`

- **Özellik geliştirme ve hata düzeltme branch'leri:**
    - `feature/{özellik-adı}` → Yeni bir özellik eklerken
    - `bugfix/{hata-adı}` → Hata düzeltmeleri için
    - `hotfix/{acil-düzeltme-adı}` → Acil düzeltmeler için (doğrudan `main` veya `develop`'a merge edilir)

## ✍️ Kod Stili ve Açıklamalar

- Tüm kodlar okunabilir ve anlaşılır olmalıdır.
- Her fonksiyon ve kritik işlem için **yorum satırları** ekleyin.
- Aşağıdaki gibi JavaDoc formatında açıklamalar kullanın:
  ```java
  /**
   * Verilen miktar ve fiyatı çarparak toplam tutarı hesaplar.
   * 
   * @param miktar Ürün adedi
   * @param fiyat Ürün birim fiyatı
   * @return Toplam fiyat
   */
  public double hesaplaFiyat(int miktar, double fiyat) {
      return miktar * fiyat;
  }
  ```
- Checkstyle, PMD veya SpotBugs gibi kod analiz araçlarını kullanarak kod kalitesini koruyun.

## 🔄 Pull Request Süreci

- **Branch'te geliştirme yaptıktan sonra bir PR açın.**
- PR başlığını anlaşılır şekilde yazın. Örneğin:
    - ✅ `Feature: Kullanıcı profil sayfası eklendi`
    - ✅ `Bugfix: Ödeme sayfasındaki hata düzeltildi`
- PR içinde yapılan değişiklikleri kısaca özetleyin.
- PR açarken en az bir takım üyesinden **review (inceleme) almanız gerekmektedir.**
- Kodunuzun `develop` branch'ine merge edilebilmesi için tüm testlerin başarıyla geçmesi gerekir.
- Güncel bir sürümde çalıştığınıza emin olun, düzenli olarak branch'ınızı güncelleyin

## 📜 Commit Mesaj Formatı

Her commit mesajı aşağıdaki formatta olmalıdır:

```
Tür: Kısa açıklama (50 karakteri geçmemeli)

Detaylı açıklama: (Opsiyonel, 72 karakterlik satırlar halinde)
```

**Tür (Commit Tipleri):**
- `feat:` Yeni bir özellik ekleme
- `fix:` Hata düzeltme
- `refactor:` Kodda değişiklik, ancak işlevsellik değişmiyor
- `docs:` Dokümantasyon değişiklikleri
- `test:` Test ekleme/güncelleme
- `chore:` Yapılandırma veya bağımlılık güncellemeleri

📌 **Örnek Commit:**
```
feat: Kullanıcı giriş ekranı eklendi

- Kullanıcı giriş ekranı tasarlandı
- Kimlik doğrulama servisi ile entegrasyon sağlandı
```

## ✅ Testler ve Kod İnceleme

- Her yeni özellik veya hata düzeltmesi için ilgili testleri yazmalısınız.
- JUnit ve Mockito gibi test framework'lerini kullanarak testlerinizi oluşturun.
- PR açılmadan önce tüm testlerin başarıyla çalıştığından emin olun.
- Kod inceleme sürecinde verilen geri bildirimlere dikkat edin ve gerekli düzeltmeleri yapın.

---

Bu kurallara uyarak projemizi daha iyi hale getirmemize yardımcı olduğunuz için teşekkür ederiz! 🚀

