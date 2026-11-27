# Remote AC – Panasonic CS-YN9SKJ (Android + IR Blaster)

Aplikasi Android untuk mengontrol AC Panasonic lewat IR blaster bawaan HP.

**Fitur:** On/Off, suhu 16–30°C, kipas Auto/1–5, air swing Auto/1–5, timer mati otomatis, jadwal mingguan (tetap jalan walau aplikasi ditutup), dan estimasi pemakaian listrik (jam nyala, kWh, rupiah, grafik 7 hari).

## Cara membuat APK

### Pilihan A — Tanpa komputer kuat (GitHub, gratis)
1. Buat akun di github.com, lalu buat repository baru (misalnya `remote-ac`).
2. Upload **semua isi folder ini**, termasuk folder `.github`. Caranya: Add file → Upload files.
3. Buka tab **Actions**. Build akan jalan otomatis, sekitar 3–5 menit.
4. Setelah centangnya hijau, klik build tersebut → bagian **Artifacts** → unduh **RemoteAC-apk** (isinya file zip berisi `app-debug.apk`).
5. Pindahkan APK ke HP lalu instal. Kalau diminta, izinkan "Instal dari sumber tidak dikenal".

### Pilihan B — Android Studio (di laptop)
1. Instal Android Studio (gratis).
2. File → Open → pilih folder `RemoteAC`, lalu tunggu proses Gradle sync selesai.
3. Sambungkan HP dengan kabel USB, dan aktifkan USB debugging di Opsi Pengembang.
4. Tekan tombol ▶ Run.

## Pertama kali dipakai
1. Arahkan **ujung atas HP** (tempat IR blaster) ke AC, dari jarak 1–5 meter.
2. Tekan **Nyalakan**.
3. Kalau AC tidak merespons, buka **Pengaturan IR** → ganti ke varian **B** → tekan **Tes kirim sinyal**.
4. Di Android 12 ke atas, tekan tombol **Izinkan alarm tepat waktu** supaya jadwal jalan tepat di menitnya.
5. Untuk HP Xiaomi/Redmi/Poco: buka Pengaturan → Aplikasi → Remote AC → aktifkan **Autostart**, lalu ubah Penghemat baterai menjadi **Tanpa batasan**. Tanpa ini, jadwal bisa tidak jalan saat aplikasi ditutup.

## Catatan
- Kode IR disusun berdasarkan protokol Panasonic AC (216-bit) dari library open-source IRremoteESP8266.
- Angka listrik adalah **estimasi** dari daya 800 W (1 PK, seri YN). Untuk angka yang akurat, pakai smart plug yang punya pengukur daya.
- Sinyal IR dari HP hanya bekerja di dalam ruangan yang sama dengan AC. Untuk mengontrol dari luar rumah, perlu IR blaster WiFi.

## Struktur kode
- `app/src/main/assets/index.html` — tampilan aplikasi (HTML/JS)
- `PanasonicIr.kt` — penyusun kode IR Panasonic dan pengirimnya
- `Scheduler.kt`, `Receivers.kt` — jadwal & timer lewat AlarmManager
- `MainActivity.kt` — WebView + jembatan JavaScript (`window.Android`)
