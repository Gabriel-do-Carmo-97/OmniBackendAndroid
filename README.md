# OmniBackendAndroid

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.5.0-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![AGP](https://img.shields.io/badge/AGP-9.3.2-blue)](https://developer.android.com/build)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Hexagonal%20%2B%20Multi--Provider-blueviolet)](#architecture)

**OmniBackend Android** é uma solução empresarial multi-provedor de abstração Backend-as-a-Service (BaaS) para aplicativos Android. Desacopla o aplicativo de provedores específicos (Firebase, Supabase, Appwrite, Back4App, PocketBase, Cloudflare, AWS Amplify, Custom REST) por meio de contratos de domínio limpos, reativos e totalmente agnósticos.

---

## 🏛️ Arquitetura do Projeto

```mermaid
graph TD
    BL["build-logic (Composite Build Conventions)"] -.->|configura| App[":app"]
    BL -.->|configura| Core[":core"]
    BL -.->|configura| BF[":backend-firebase"]
    BL -.->|configura| BS[":backend-supabase"]
    BL -.->|configura| BA[":backend-appwrite"]
    BL -.->|configura| BB[":backend-back4app"]
    BL -.->|configura| BP[":backend-pocketbase"]
    BL -.->|configura| BC[":backend-cloudflare"]
    BL -.->|configura| BAM[":backend-amplify"]
    BL -.->|configura| BR[":backend-rest"]

    App -->|consome contratos| Core
    App -->|injeta provedor| BF
    App -->|injeta provedor| BS
    App -->|injeta provedor| BA
    App -->|injeta provedor| BB
    App -->|injeta provedor| BP
    App -->|injeta provedor| BC
    App -->|injeta provedor| BAM
    App -->|injeta provedor| BR

    BF -->|implementa contratos| Core
    BS -->|implementa contratos| Core
    BA -->|implementa contratos| Core
    BB -->|implementa contratos| Core
    BP -->|implementa contratos| Core
    BC -->|implementa contratos| Core
    BAM -->|implementa contratos| Core
    BR -->|implementa contratos| Core

    subgraph "Módulo Agnóstico (:core)"
        Core --> Contracts["AuthRepository, FirestoreRepository, StorageRepository..."]
        Core --> DomainModels["OmniUser, DataResult<T>, AppError"]
    end
```

---

## 📦 Módulos do Framework

| Módulo | Tipo | Documentação | Descrição |
| :--- | :---: | :---: | :--- |
| **`build-logic`** | Composite Build | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/build-logic/README.md) | Convention Plugins Gradle unificando AGP 9.3.2 e Kotlin. |
| **`:core`** | Android Library | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/core/README.md) | Núcleo agnóstico contendo contratos, modelos `OmniUser`, `DataResult` e `AppError`. **Zero dependências externas.** |
| **`:backend-firebase`** | Android Library | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/backend-firebase/README.md) | Driver do Google Firebase (Auth, Firestore, RTDB, Storage, Telemetria, AppCheck, Vertex AI). |
| **`:backend-supabase`** | Android Library | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/backend-supabase/README.md) | Driver do Supabase (GoTrue Auth, PostgREST, Storage, Realtime). |
| **`:backend-appwrite`** | Android Library | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/backend-appwrite/README.md) | Driver do Appwrite (Account, Databases & Realtime WebSockets, Storage). |
| **`:backend-back4app`** | Android Library | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/backend-back4app/README.md) | Driver do Back4App / Parse Platform (`ParseUser`, `ParseObject`, `ParseFile`). |
| **`:backend-pocketbase`** | Android Library | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/backend-pocketbase/README.md) | Driver do PocketBase (RecordAuth, Collections, Files). |
| **`:backend-cloudflare`** | Android Library | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/backend-cloudflare/README.md) | Driver da Cloudflare (Workers Auth/API, D1 Database, R2 Storage). |
| **`:backend-amplify`** | Android Library | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/backend-amplify/README.md) | Driver da AWS Amplify (Cognito, DynamoDB/AppSync, S3 Storage). |
| **`:backend-rest`** | Android Library | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/backend-rest/README.md) | Driver REST customizado/proprietário com suporte a JWT e RFC 7807. |
| **`:app`** | Android Application | [README.md](file:///C:/Users/gcarm/AndroidStudioProjects/OmniBackendAndroid/app/README.md) | Aplicativo de demonstração consumindo os contratos do `:core` e trocando de provedor. |

---

## ⚡ Guia Rápido de Inicialização

### 1. Selecionar o Provedor Desejado

No `build.gradle.kts` do seu aplicativo (`:app`):

```kotlin
dependencies {
    implementation(project(":core"))
    implementation(project(":backend-firebase")) // ou :backend-supabase, :backend-appwrite, etc.
}
```

### 2. Inicializar a Facade no `Application.onCreate()`

```kotlin
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Exemplo: Inicializando o Firebase
        OmniFirebase.initialize(context = this)

        // Ou Supabase: OmniSupabase.initialize(this, url = "...", anonKey = "...")
        // Ou Appwrite: OmniAppwrite.initialize(this, endpoint = "...", projectId = "...", databaseId = "...")
    }
}
```

### 3. Consumir a Camada Agnóstica de Domínio

```kotlin
class UserViewModel(
    private val authRepository: AuthRepository // Injetado via Hilt, Koin ou Facade
) : ViewModel() {

    val userFlow: Flow<OmniUser?> = authRepository.authState

    suspend fun login(email: String, pass: String) {
        val result: DataResult<OmniUser> = authRepository.login(email, pass)
        when (result) {
            is DataResult.Success -> println("Usuário autenticado: ${result.data.displayName}")
            is DataResult.Failure -> println("Erro: ${result.error}")
        }
    }
}
```

---

## 🧪 Testes e Compilação

Executar a suíte completa de testes unitários em todos os módulos:

```bash
./gradlew testDebugUnitTest
```

Gerar documentação em HTML das APIs com o Dokka:

```bash
./gradlew dokkaGenerate
```
