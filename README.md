# OmniBackendAndroid

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.5.0-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![AGP](https://img.shields.io/badge/AGP-9.3.2-blue)](https://developer.android.com/build)
[![Architecture](https://img.shields.io/badge/Architecture-Multi--Provider%20BaaS-blueviolet)](#architecture)

OmniBackend Android is an enterprise-ready, multi-provider Backend-as-a-Service (BaaS) abstraction architecture for Android applications. It enables applications to consume cloud providers (Firebase, Supabase, etc.) with clean, reactive, and unified interfaces.

---

## 🏛️ Project Architecture

```mermaid
graph TD
    App[":app (Client Application / Presentation)"] -->|depends on| BF[":backend-firebase"]

    subgraph "OmniBackend Architecture"
        BF --> Facade["OmniFirebase (Unified Facade)"]
        BF --> Security["AppCheckManager (Play Integrity / Debug)"]
        BF --> Telemetry["FirebaseTelemetry (Crashlytics + Performance)"]
        BF --> Repos["Domain & Data Repositories"]

        Repos --> Auth["AuthRepository (Flow<FirebaseUser?>)"]
        Repos --> Firestore["FirestoreRepository"]
        Repos --> RTDB["RealtimeDatabaseRepository"]
        Repos --> Storage["StorageRepository (Streams/Uri)"]
        Repos --> Config["RemoteConfigRepository (Reactive)"]
        Repos --> Analytics["AnalyticsRepository"]
        Repos --> VertexAI["VertexAIRepository (Gemini AI)"]
        Repos --> Realtime["Geolocation, Messaging, Presence"]
    end

    BF --> GoogleFirebase["Google Firebase SDK (BoM 33.9.0)"]
```

---

## 🚀 Providers

| Provider Module | Status | Features |
| :--- | :---: | :--- |
| **`:backend-firebase`** | ✅ **Active** | Auth (reactive), Firestore, Realtime DB, Storage, Config, Analytics, Gemini AI, App Check (Play Integrity), Telemetry |
| **`:backend-supabase`** | 🔜 *Planned* | GoTrue Auth, PostgREST, Storage, Realtime CDC |

---

## ⚡ Quick Start with `:backend-firebase`

### 1. Add dependency in your `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":backend-firebase"))
}
```

### 2. Initialize in `Application` or `MainActivity`:

```kotlin
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        OmniFirebase.initialize(
            context = this,
            enableAppCheck = true,
            isDebug = BuildConfig.DEBUG
        )
    }
}
```

### 3. Consume via `OmniFirebase` Facade:

```kotlin
class UserProfileViewModel : ViewModel() {

    // Reactive user session
    val currentUser = OmniFirebase.auth.authState

    // Firestore CRUD
    suspend fun saveProfile(user: UserProfile) {
        OmniFirebase.firestore.addDocument("users", user, user.id)
    }

    // Telemetry & Error reporting
    fun handleError(error: AppError) {
        OmniFirebase.telemetry.recordError(error)
    }
}
```

---

## 🧪 Testing

Execute tests across all modules:

```bash
./gradlew testDebugUnitTest
```

Execute tests specifically for `:backend-firebase`:

```bash
./gradlew :backend-firebase:testDebugUnitTest
```
