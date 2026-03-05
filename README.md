# ◈ CipherSMS — Secure Military-Grade Messaging

> **Encrypted · Private · Unstoppable**

A professional, feature-rich SMS/RCS messaging app with a military intelligence aesthetic — surpassing Google Messages in features and security.

---

## 🎨 Design Theme

- **Black + Neon Green** military/intelligence aesthetic
- Digital grid backgrounds with scan line animations
- Glowing elements and pulsing effects
- Monospace terminal fonts throughout
- Full RTL (Arabic) support

---

## 🚀 Features

### Messaging
| Feature | Status |
|---------|--------|
| SMS Send/Receive | ✅ |
| MMS (Media) | ✅ |
| RCS (Read/Deliver status) | ✅ |
| Message Status (Sent/Delivered/Read) | ✅ |
| Typing Indicator | ✅ |
| Disappearing Messages | ✅ |
| Message Scheduling | ✅ |
| Edit Message (RCS, 5 min) | ✅ |
| Reply to Message | ✅ |
| Emoji Reactions | ✅ |

### Security 🔐
| Feature | Status |
|---------|--------|
| End-to-End Encryption (AES-256-GCM) | ✅ |
| PIN Lock | ✅ |
| Biometric (Fingerprint) | ✅ |
| Secret Vault | ✅ |
| Screenshot Protection | ✅ |
| Stealth Mode (no read receipts) | ✅ |
| Message Blur Mode | ✅ |
| Auto-Delete | ✅ |
| Spam Filter (AI) | ✅ |

### AI Features 🤖
| Feature | Status |
|---------|--------|
| Smart Replies | ✅ |
| Conversation Summary | ✅ |
| Spam Detection | ✅ |
| Message Translation | ✅ |
| Fraud Detection | ✅ |

### Advanced
| Feature | Status |
|---------|--------|
| Schedule Messages | ✅ |
| Conversation Search | ✅ |
| Pin/Archive Conversations | ✅ |
| Personal/Work Categories | ✅ |
| Multi-SIM Support | ✅ |
| QR Code Encrypted Messages | ✅ |
| Saved Messages (self-send) | ✅ |

---

## 🏗️ Architecture

```
com.ciphersms.app/
├── data/
│   ├── local/         # Room Database, DAOs, Entities
│   └── repository/    # Repository implementations
├── domain/
│   ├── model/         # Domain models
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Use cases
├── presentation/
│   ├── ui/            # Compose screens
│   ├── viewmodel/     # ViewModels
│   └── theme/         # CipherTheme
├── di/                # Hilt modules
├── security/          # Encryption, Biometric
├── service/           # Background services
├── receiver/          # SMS/MMS/Boot receivers
└── worker/            # WorkManager tasks
```

**Patterns:** Clean Architecture · Repository · MVVM · Dependency Injection (Hilt)

---

## ⚙️ Setup in Android Studio

### Requirements
- Android Studio Hedgehog or newer
- JDK 17
- Minimum SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)

### Steps

1. **Clone/Open** the project in Android Studio

2. **Sync Gradle** — File → Sync Project with Gradle Files

3. **Set Default SMS App** — The app needs to be set as default SMS app on device

4. **Required Permissions** — Grant on first launch:
   - READ_SMS, SEND_SMS, RECEIVE_SMS
   - READ_CONTACTS
   - INTERNET
   - CAMERA, RECORD_AUDIO
   - READ_MEDIA_IMAGES, READ_MEDIA_VIDEO
   - POST_NOTIFICATIONS (Android 13+)

5. **Build** → Run 'app'

---

## 📦 Key Dependencies

| Library | Purpose |
|---------|---------|
| Jetpack Compose + Material3 | UI |
| Hilt | Dependency Injection |
| Room + Paging 3 | Database |
| WorkManager | Scheduled messages |
| Security Crypto | Encrypted preferences |
| Biometric | Fingerprint auth |
| Coil | Image loading |
| DataStore | Settings |
| Accompanist | Permissions, SystemUI |

---

## 🔐 Security Architecture

- **AES-256-GCM** encryption via Android Keystore
- **Encrypted SharedPreferences** for sensitive data
- **PIN hashing** with SHA-256 + salt
- **Biometric** via AndroidX Biometric API
- **No cleartext traffic** (network_security_config)
- **Backup exclusion** for sensitive files

---

## 🌍 Localization

- English (default)
- Arabic (RTL) — full `values-ar` strings

---

*CipherSMS — Your messages. Your privacy. Your fortress.*
