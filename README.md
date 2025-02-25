# 📱 VaultMessenger

VaultMessenger is a secure and intuitive chat application designed for seamless real-time communication. Built with Kotlin, Jetpack Compose, and Firebase, the app ensures encrypted messaging, media sharing, and robust user presence management.

---

## 🚀 Features

- **Real-time Messaging:** Instant chat updates using Firestore and Flow.
- **User Authentication:** Firebase Authentication for secure login and registration.
- **Media Sharing:** Send and receive images, voice notes, and more.
- **Typing Indicators:** Visual feedback when someone is typing.
- **Online Status:** See when users are online or offline.
- **Push Notifications:** Firebase Cloud Messaging for in-app and background notifications.
- **Customizable UI:** Built with Jetpack Compose for a modern, responsive interface.
- **Secure Data:** Integration with Firebase App Check and SHA-256 encryption.

---

## 🛠️ Tech Stack

- **Frontend:** Kotlin, Jetpack Compose
- **Backend:** Firebase (Authentication, Firestore, Cloud Functions, Storage)
- **Push Notifications:** Firebase Cloud Messaging (FCM)
- **Security:** Firebase App Check, SHA-256 encryption
- **Cloud Tasks:** Google Cloud Tasks for scheduling and queuing tasks

---

## 📲 Screenshots

| Chat Screen | Image Upload | Voice Note |
|-------------|--------------|------------|
| ![Chat Screen](link_to_image) | ![Image Upload](link_to_image) | ![Voice Note](link_to_image) |

---

## ⚙️ Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/vaultGideonProject/app.git
   ```
2. **Open with Android Studio**
3. **Add Firebase to your project:**
   - Download `google-services.json` from Firebase Console
   - Place it in the `app/` directory
4. **Run the app:**
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🔐 Environment Variables

Create a `.env` file in the root directory and add the following:

```plaintext
FIREBASE_API_KEY=your_api_key
FIREBASE_AUTH_DOMAIN=your_auth_domain
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_STORAGE_BUCKET=your_storage_bucket
FIREBASE_MESSAGING_SENDER_ID=your_sender_id
FIREBASE_APP_ID=your_app_id
```

---

## 📚 Usage

- **Login or Register** using Firebase Authentication
- **Start a new chat** by selecting a contact
- **Send images or voice notes** directly from the chat screen
- **Receive notifications** for new messages and typing indicators
- **Monitor user presence** with real-time online/offline indicators

---

## 🧩 Project Structure

```plaintext
📦 VaultMessenger
├── 📂 app
│   ├── 📂 src
│   │   ├── 📂 main
│   │   │   ├── 📂 java/com/example/vaultmessenger
│   │   │   │   ├── 📂 auth
│   │   │   │   ├── 📂 chat
│   │   │   │   ├── 📂 data
│   │   │   │   ├── 📂 ui
│   │   │   │   └── MainActivity.kt
├── 📄 .env
├── 📄 build.gradle
└── 📄 README.md
```

---

## ✅ Roadmap

- [ ] Add end-to-end encryption
- [ ] Implement voice and video calling
- [ ] Enhance UI animations
- [ ] Multi-language support

---

## 🤝 Contributing

Contributions are welcome! If you'd like to improve VaultMessenger, please follow these steps:

1. Fork the repository
2. Create a new branch (`feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License. See the `LICENSE` file for details.

---

## 📧 Contact

Created by **David Mabaso** — feel free to reach out!

- LinkedIn: [David Mabaso](https://www.linkedin.com/in/david-mabaso-360140331/)

---

**Star** ⭐ this repo if you found it helpful!

---
