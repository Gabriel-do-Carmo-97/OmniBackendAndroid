# OmniBackendAndroid 🚀

[![Android CI/CD](https://github.com/Gabriel-do-Carmo-97/OmniBackendAndroid/actions/workflows/android.yml/badge.svg)](https://github.com/Gabriel-do-Carmo-97/OmniBackendAndroid/actions)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.7.1-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![AGP](https://img.shields.io/badge/AGP-9.3.2-blue)](https://developer.android.com/build)
[![Quality](https://img.shields.io/badge/Detekt-0%20issues-success.svg)](#qualidade-e-governança)

**OmniBackend Android** é um framework empresarial e agnóstico de abstração Backend-as-a-Service (BaaS) e orquestração de nuvem para Android. 

Permite que aplicativos corporativos operem com múltiplos provedores de backend (Firebase, Supabase, Appwrite, Back4App, PocketBase, Cloudflare, AWS Amplify e REST proprietário) com **Zero SDK Leakage** e suporte nativo a **Failover Automático / Redundância Ativo-Passivo**.

---

## 🏛️ Arquitetura Multi-Módulo Corporativa

```mermaid
graph TD
    classDef core fill:#FBBC04,stroke:#F29900,stroke-width:2px,color:#000;
    classDef backend fill:#4285F4,stroke:#1A73E8,stroke-width:2px,color:#fff;
    classDef bundle fill:#9C27B0,stroke:#7B1FA2,stroke-width:2px,color:#fff;

    Core[":core<br/>(Contratos Agnósticos, Modelos, Tracing, FailoverRouter)"]:::core

    subgraph Drivers [Diretório: backend/]
        BF[":backend:firebase"]:::backend
        BS[":backend:supabase"]:::backend
        BA[":backend:appwrite"]:::backend
        BP[":backend:pocketbase"]:::backend
        BB[":backend:back4app"]:::backend
        BAM[":backend:amplify"]:::backend
        BR[":backend:rest"]:::backend
        BC[":backend:cloudflare"]:::backend
    end

    BF -->|implementa| Core
    BS -->|implementa| Core
    BA -->|implementa| Core
    BP -->|implementa| Core
    BB -->|implementa| Core
    BAM -->|implementa| Core
    BR -->|implementa| Core
    BC -->|implementa| Core

    subgraph Bundles [Diretório: bundle/]
        BHybrid[":bundle:hybrid<br/>(Failover Dinâmico entre quaisquer 2 backends)"]:::bundle
        BSelf[":bundle:self-hosted<br/>(PocketBase + Appwrite + Hybrid)"]:::bundle
        BCloud[":bundle:cloud-native<br/>(Firebase + Supabase + Amplify + Hybrid)"]:::bundle
        BEnterprise[":bundle:enterprise-hybrid<br/>(REST Proprietário + Firebase + Hybrid)"]:::bundle
        BEdge[":bundle:edge-serverless<br/>(Cloudflare + Supabase + Hybrid)"]:::bundle
        BClassic[":bundle:baas-classic<br/>(Back4App + Firebase + Hybrid)"]:::bundle
        BAll[":bundle:all<br/>(Guarda-chuva completo com todos os 8 backends)"]:::bundle
    end

    BHybrid -->|orquestra| Core
    BSelf -->|api| BP
    BSelf -->|api| BA
    BSelf -->|api| BHybrid

    BCloud -->|api| BF
    BCloud -->|api| BS
    BCloud -->|api| BAM
    BCloud -->|api| BHybrid

    BEnterprise -->|api| BR
    BEnterprise -->|api| BF
    BEnterprise -->|api| BHybrid

    BEdge -->|api| BC
    BEdge -->|api| BS
    BEdge -->|api| BHybrid

    BClassic -->|api| BB
    BClassic -->|api| BF
    BClassic -->|api| BHybrid

    BAll -->|api| BF
    BAll -->|api| BS
    BAll -->|api| BA
    BAll -->|api| BP
    BAll -->|api| BB
    BAll -->|api| BAM
    BAll -->|api| BR
    BAll -->|api| BC
    BAll -->|api| BHybrid
```

---

## 📦 Matriz de Módulos e Artefatos

### 1. Núcleo Agnóstico (`core/`)
| Módulo | Artefato Maven | Descrição |
| :--- | :--- | :--- |
| **`:core`** | `br.wgc.omnibackend:core` | Contratos puros (`AuthRepository`, `FirestoreRepository`, `StorageRepository`), modelos `OmniUser`, `DataResult`, `AppError`, tracing distribuído e enfileiramento offline. |

### 2. Drivers Especializados (`backend/`)
| Módulo | Artefato Maven | Provedor / Nuvem | Capacidades Principais |
| :--- | :--- | :--- | :--- |
| **`:backend:firebase`** | `br.wgc.omnibackend:backend-firebase` | Google Cloud | Auth, Firestore, RTDB, Storage, Telemetria, AppCheck, Vertex AI. |
| **`:backend:supabase`** | `br.wgc.omnibackend:backend-supabase` | PostgreSQL / Supabase | GoTrue Auth, PostgREST CRUD, Realtime WebSocket, Storage. |
| **`:backend:appwrite`** | `br.wgc.omnibackend:backend-appwrite` | Appwrite Cloud / Docker | Account, Databases, Realtime Subscriptions, Storage. |
| **`:backend:pocketbase`** | `br.wgc.omnibackend:backend-pocketbase` | PocketBase (Go / SQLite) | RecordAuth, Collections CRUD, File Storage. |
| **`:backend:back4app`** | `br.wgc.omnibackend:backend-back4app` | Back4App / Parse | ParseUser, ParseObject Queries, ParseFile Storage. |
| **`:backend:amplify`** | `br.wgc.omnibackend:backend-amplify` | Amazon Web Services | AWS Cognito, DynamoDB API, S3 Storage. |
| **`:backend:rest`** | `br.wgc.omnibackend:backend-rest` | API Proprietária | JWT Auth, RFC 7807 ProblemDetails, Endpoints REST. |
| **`:backend:cloudflare`** | `br.wgc.omnibackend:backend-cloudflare` | Cloudflare Edge | Workers Auth, D1 SQL Database, R2 Object Storage. |

### 3. Bundles Agregadores (`bundle/`)
| Módulo | Artefato Maven | Composição | Propósito / Caso de Uso |
| :--- | :--- | :--- | :--- |
| **`:bundle:hybrid`** | `br.wgc.omnibackend:bundle-hybrid` | Contratos Core + FailoverRouter | Motor de failover e roteamento agnóstico (`OmniHybrid`). Permite combinar **quaisquer dois provedores**. |
| **`:bundle:self-hosted`** | `br.wgc.omnibackend:bundle-self-hosted` | PocketBase + Appwrite + Hybrid | Solução para empresas que usam servidores próprios / VPS (Zero dependência de Big Techs). |
| **`:bundle:cloud-native`** | `br.wgc.omnibackend:bundle-cloud-native` | Firebase + Supabase + AWS Amplify + Hybrid | Multi-cloud em escala (Google Cloud + AWS + Supabase com redundância). |
| **`:bundle:enterprise-hybrid`** | `br.wgc.omnibackend:bundle-enterprise-hybrid` | REST Proprietário + Firebase + Hybrid | Corporativo tradicional: API REST própria como primário e nuvem como contingência. |
| **`:bundle:edge-serverless`** | `br.wgc.omnibackend:bundle-edge-serverless` | Cloudflare + Supabase + Hybrid | Ultra-baixa latência (Edge Workers/D1/R2) com robustez relacional Postgres. |
| **`:bundle:baas-classic`** | `br.wgc.omnibackend:bundle-baas-classic` | Back4App (Parse) + Firebase + Hybrid | BaaS clássico NoSQL/Relacional para apps existentes ou migrações suaves. |
| **`:bundle:all`** | `br.wgc.omnibackend:bundle-all` | Todos os 8 Drivers + Hybrid | Pacote completo com todos os 8 provedores integrados. |

---

## ⚡ Instalação & Consumo via GitHub Packages

Adicione o repositório no seu `settings.gradle.kts`:

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

### Exemplo 1: Uso com Failover Ativo-Passivo (Redundância)
```kotlin
// build.gradle.kts do seu app:
dependencies {
    implementation("br.wgc.omnibackend:bundle-hybrid:0.0.x")
    implementation("br.wgc.omnibackend:backend-firebase:0.0.x")
    implementation("br.wgc.omnibackend:backend-supabase:0.0.x")
}

// No seu Application ou Módulo DI:
val database = OmniHybrid.createDatabase(
    primary = OmniFirebase.firestore,
    secondary = OmniSupabase.database
)

// Se o Firebase falhar ou der timeout de rede, o Supabase assume transparentemente!
```

---

## 🛡️ Qualidade e Governança
* **Detekt Static Analysis**: Regras estritas sem supressões não justificadas.
* **100% KDoc**: Toda classe, método e contrato público documentado.
* **Zero SDK Leakage**: Interfaces do `:core` isolam completamente os SDKs de terceiros.
* **Conventional Commits**: Padronização estrita de PRs e tags automáticas (`v0.0.<run_number>`).

Consulte [CONTRIBUTING.md](CONTRIBUTING.md) para diretrizes de desenvolvimento e [SECURITY.md](SECURITY.md) para nossa política de divulgação responsável de vulnerabilidades.
