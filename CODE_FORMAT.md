# **IntelliJ IDEA İçin Clang-Format Kullanım Kılavuzu**

Bu belge, **Java kodlarını Clang-Format kullanarak IntelliJ IDEA'da biçimlendirme** adımlarını içerir.

---

## **📌 Gereksinimler**

- IntelliJ IDEA (Community veya Ultimate) kurulmuş olmalı.
- **`.clang-format`** dosyası proje dizininde bulunmalı.
- (Opsiyonel) Clang-Format terminalden çalıştırmak için sistemde kurulu olmalı.
- Clang-format kurmak için `pip install clang-format`

---

## **🛠 Kurulum ve Ayarlar**

### **1️⃣ Clang-Format Eklentisini Yükleyin**

1. **IntelliJ IDEA'yı açın.**
2. **`Dosya` → `Ayarlar`** menüsüne girin (**Mac:** `Preferences`).
3. **`Eklentiler` (Plugins)** sekmesine gidin.
4. **"Clang-Format"** eklentisini aratıp yükleyin.
5. IntelliJ IDEA'yı **yeniden başlatın**.

---

## **1️⃣ Kaydederken Otomatik Formatlamayı Açın**

1. **IntelliJ IDEA’yı açın**.
2. **`Dosya` → `Ayarlar`** (`Preferences` **Mac için**) menüsüne girin.
3. **`Araçlar` → `Dosya Kaydetme Eylemleri (Actions on Save)`** sekmesine gidin.
4. **Reformat code** seçeneğini **işaretleyin**.
5. **"Uygula" → "Tamam"** butonlarına basarak ayarları kaydedin.


