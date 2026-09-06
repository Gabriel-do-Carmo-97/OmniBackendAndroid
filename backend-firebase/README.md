# 🔥 Módulo :backend-firebase — Driver Firebase

O módulo **`:backend-firebase`** é a implementação concreta dos contratos do `:core` utilizando o SDK oficial do **Google Firebase (BoM 33.9.0)**.

---

## 🛠️ Recursos Suportados

- **Autenticação:** Firebase Auth (`email/senha`, `Google Sign-In`, `login anônimo`).
- **Banco de Dados:** Cloud Firestore e Firebase Realtime Database.
- **Armazenamento:** Firebase Storage.
- **Segurança:** App Check (Play Integrity e Provider de Debug).
- **Telemetria:** Firebase Analytics, Crashlytics e Performance Monitoring via `FirebaseTelemetry`.
- **IA:** Vertex AI for Firebase.

---

## ⚡ Exemplo de Inicialização

```kotlin
OmniFirebase.initialize(
    context = applicationContext,
    enableAppCheck = true,
    isDebug = BuildConfig.DEBUG
)

// Acesso aos repositórios
val auth = OmniFirebase.auth
val db = OmniFirebase.firestore
val storage = OmniFirebase.storage
```

---

## 🧪 Testes Unitários

```bash
./gradlew :backend-firebase:testDebugUnitTest
```
