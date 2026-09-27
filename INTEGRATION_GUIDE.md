# 📘 Guia de Integração Corporativo — OmniBackend Android

Bem-vindo ao **OmniBackend Android**! Este guia fornece aos desenvolvedores e squads todas as diretrizes, padrões e exemplos práticos para integrar a infraestrutura BaaS e nuvem em seus aplicativos Android.

---

## 🧭 Visão Geral

O **OmniBackend Android** resolve o acoplamento de aplicações a provedores de nuvem específicos por meio de:
1. **Contratos Agnósticos:** Sua feature consome `AuthRepository`, `FirestoreRepository` ou `StorageRepository` sem saber qual nuvem está por trás.
2. **Injeção Transparente via Hilt:** Basta importar o bundle desejado e os módulos Dagger/Hilt injetam as instâncias automaticamente.
3. **Módulo de Testes Isolado (`:testing`):** Squads realizam testes unitários e instrumentados usando fakes determinísticos em memória sem precisar de emuladores ou credenciais de nuvem.

---

## 🚀 Como Configurar no Projeto Consumidor

### 1. Adicionar o Repositório de Pacotes

No arquivo `settings.gradle.kts` do seu projeto:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Gabriel-do-Carmo-97/OmniBackendAndroid")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

---

### 2. Escolher a Estratégia de Consumo

#### Estratégia A: Por Bundles Temáticos (Recomendada para Produtividade)

Os bundles agregadores incluem os drivers correspondentes e a amarração de injeção de dependência Hilt pronta:

```kotlin
dependencies {
    // Exemplo 1: Alta Disponibilidade Multi-Nuvem (Firebase + Supabase + Appwrite)
    implementation("br.wgc.omnibackend:bundle-hybrid:1.0.0")

    // Exemplo 2: Nuvens Públicas Globais (Firebase + Supabase + AWS Amplify + Cloudflare)
    // implementation("br.wgc.omnibackend:bundle-cloud-native:1.0.0")

    // Exemplo 3: Auto-Hospedado / LGPD (Supabase + Appwrite + PocketBase + Back4App)
    // implementation("br.wgc.omnibackend:bundle-self-hosted:1.0.0")

    // Exemplo 4: Par Direto GCP + AWS (Firebase + AWS Amplify)
    // implementation("br.wgc.omnibackend:bundle-firebase-amplify:1.0.0")
}
```

#### Estratégia B: Drivers Atômicos Granulares (Para Tamanho Mínimo de APK)

Se sua squad utiliza apenas um provedor específico e deseja evitar dependências desnecessárias:

```kotlin
dependencies {
    // Contratos agnósticos fundamentais
    implementation("br.wgc.omnibackend:core:1.0.0")

    // Apenas o driver desejado
    implementation("br.wgc.omnibackend:backend-supabase:1.0.0")
    // ou: backend-firebase, backend-amplify, backend-appwrite, backend-cloudflare, backend-pocketbase, backend-back4app, backend-rest
}
```

#### Estratégia C: Módulo de Fakes para Testes Unitários

Para módulos de domínio ou testes de ViewModel sem acoplamento a rede:

```kotlin
dependencies {
    testImplementation("br.wgc.omnibackend:testing:1.0.0")
}
```

---

## 💻 Exemplos Práticos de Uso

### 1. Injeção de Dependências com Hilt

Basta injetar as interfaces puras no seu repositório ou ViewModel:

```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository,
) : ViewModel() {

    private val _userState = MutableStateFlow<OmniUser?>(null)
    val userState: StateFlow<OmniUser?> = _userState.asStateFlow()

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            when (val result = authRepository.loginWithEmail(email, pass)) {
                is DataResult.Success -> {
                    _userState.value = result.data
                }
                is DataResult.Error -> {
                    // Tratar erro tipado agnóstico (AppError)
                }
                DataResult.Loading -> Unit
            }
        }
    }
}
```

### 2. Uso dos Fakes em Testes Unitários

```kotlin
class AuthViewModelTest {

    private val fakeAuth = FakeAuthRepository()
    private val fakeDb = FakeFirestoreRepository()

    @Test
    fun `when login succeeds, updates user state`() = runTest {
        val viewModel = AuthViewModel(fakeAuth, fakeDb)

        fakeAuth.fakeUser = OmniUser(id = "user_123", email = "dev@empresa.com.br")

        viewModel.signIn("dev@empresa.com.br", "password")

        assertEquals("user_123", viewModel.userState.value?.id)
    }
}
```

---

## 📋 Catálogo de Coordenadas Maven

| Módulo | Coordenada Maven |
| :--- | :--- |
| **Core (Agnóstico)** | `br.wgc.omnibackend:core:<versao>` |
| **Testing (Fakes)** | `br.wgc.omnibackend:testing:<versao>` |
| **Driver Firebase** | `br.wgc.omnibackend:backend-firebase:<versao>` |
| **Driver Supabase** | `br.wgc.omnibackend:backend-supabase:<versao>` |
| **Driver Appwrite** | `br.wgc.omnibackend:backend-appwrite:<versao>` |
| **Driver PocketBase** | `br.wgc.omnibackend:backend-pocketbase:<versao>` |
| **Driver Back4App** | `br.wgc.omnibackend:backend-back4app:<versao>` |
| **Driver AWS Amplify** | `br.wgc.omnibackend:backend-amplify:<versao>` |
| **Driver Cloudflare** | `br.wgc.omnibackend:backend-cloudflare:<versao>` |
| **Driver Custom REST** | `br.wgc.omnibackend:backend-rest:<versao>` |
| **Bundle All** | `br.wgc.omnibackend:bundle-all:<versao>` |
| **Bundle Hybrid** | `br.wgc.omnibackend:bundle-hybrid:<versao>` |
| **Bundle Self-Hosted** | `br.wgc.omnibackend:bundle-self-hosted:<versao>` |
| **Bundle Cloud-Native** | `br.wgc.omnibackend:bundle-cloud-native:<versao>` |
| **Bundle Enterprise-Hybrid** | `br.wgc.omnibackend:bundle-enterprise-hybrid:<versao>` |
| **Bundle Edge-Serverless** | `br.wgc.omnibackend:bundle-edge-serverless:<versao>` |
| **Bundle BaaS-Classic** | `br.wgc.omnibackend:bundle-baas-classic:<versao>` |
| **Bundle Firebase-Supabase** | `br.wgc.omnibackend:bundle-firebase-supabase:<versao>` |
| **Bundle Firebase-Amplify** | `br.wgc.omnibackend:bundle-firebase-amplify:<versao>` |
| **Bundle Firebase-Back4App** | `br.wgc.omnibackend:bundle-firebase-back4app:<versao>` |
| **Bundle Supabase-Cloudflare** | `br.wgc.omnibackend:bundle-supabase-cloudflare:<versao>` |
