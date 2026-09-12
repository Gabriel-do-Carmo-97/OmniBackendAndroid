# 📦 Módulos Bundles — Agregadores Inteligentes do OmniBackend Android

A pasta **`bundle/`** contém os módulos agregadores (*Bundle Modules*) do **OmniBackend Android**. 

Em vez de importar múltiplos drivers manualmente no seu aplicativo, você importa um único **Bundle** adaptado ao perfil arquitetural e de infraestrutura do seu projeto.

---

## 🧭 Guia de Escolha: Qual Bundle Importar?

| Módulo Bundle | Qual Importar no `build.gradle.kts` | Para Que Serve? | Quando Usar? |
| :--- | :--- | :--- | :--- |
| **`all`** | `implementation(project(":bundle:all"))` <br> `implementation("br.wgc.omnibackend:bundle-all:1.0.0")` | **Suíte Completa (Todos os 8 Drivers):** Firebase, Supabase, Appwrite, PocketBase, Back4App, Amplify, Cloudflare, Custom REST. | Prototipagem, testes, super-apps ou migração ativa entre quaisquer nuvens do mercado. |
| **`hybrid`** | `implementation(project(":bundle:hybrid"))` <br> `implementation("br.wgc.omnibackend:bundle-hybrid:1.0.0")` | **Alta Disponibilidade e Failover em Tempo Real:** Firebase + Supabase + Appwrite. | Apps críticos (bancos, e-commerce, saúde) que exigem zero perda de dados e failover automático. |
| **`self-hosted`** | `implementation(project(":bundle:self-hosted"))` <br> `implementation("br.wgc.omnibackend:bundle-self-hosted:1.0.0")` | **Infraestrutura Auto-Hospedada / Open Source:** Supabase, Appwrite, PocketBase, Back4App. | Governança estrita de dados (LGPD/GDPR), redes privadas VPN ou servidor próprio (On-Premise). |
| **`cloud-native`** | `implementation(project(":bundle:cloud-native"))` <br> `implementation("br.wgc.omnibackend:bundle-cloud-native:1.0.0")` | **Nuvens Públicas Globais de Escala Massiva:** Firebase, Supabase, AWS Amplify, Cloudflare. | Startups B2C de alto crescimento, milhões de acessos simultâneos e auto-scaling elástico. |
| **`enterprise-hybrid`** | `implementation(project(":bundle:enterprise-hybrid"))` <br> `implementation("br.wgc.omnibackend:bundle-enterprise-hybrid:1.0.0")` | **Nuvem Híbrida & Sistemas Legados:** AWS Amplify, Cloudflare, Custom REST, Firebase. | Empresas com APIs REST/SOAP legadas internas que precisam se integrar com AWS, Cloudflare ou Firebase. |
| **`edge-serverless`** | `implementation(project(":bundle:edge-serverless"))` <br> `implementation("br.wgc.omnibackend:bundle-edge-serverless:1.0.0")` | **Computação em Borda de Ultra-Baixa Latência:** Cloudflare Workers/D1/R2, PocketBase, Custom REST. | Apps IoT, geolocalização em tempo real, respostas em milissegundos e serverless leve. |
| **`baas-classic`** | `implementation(project(":bundle:baas-classic"))` <br> `implementation("br.wgc.omnibackend:bundle-baas-classic:1.0.0")` | **BaaS Tradicionais para Desenvolvimento Rápido:** Firebase, Appwrite, Back4App. | Foco em máxima velocidade de entrega (Time-to-Market) usando BaaS maduros e tradicionais. |
| **`firebase-supabase`** | `implementation(project(":bundle:firebase-supabase"))` <br> `implementation("br.wgc.omnibackend:bundle-firebase-supabase:1.0.0")` | **Par Pré-Configurado:** Google Firebase + Supabase (PostgreSQL). | Failover ativo-passivo direto entre GCP Firebase e Supabase. |
| **`firebase-amplify`** | `implementation(project(":bundle:firebase-amplify"))` <br> `implementation("br.wgc.omnibackend:bundle-firebase-amplify:1.0.0")` | **Par Multi-Cloud:** Google Firebase + AWS Amplify (GCP + AWS). | Redundância corporativa direta entre os dois maiores gigantes da nuvem. |
| **`firebase-back4app`** | `implementation(project(":bundle:firebase-back4app"))` <br> `implementation("br.wgc.omnibackend:bundle-firebase-back4app:1.0.0")` | **Par Híbrido:** Google Firebase + Back4App (Parse Platform). | Contingência robusta entre ecossistema Google e infraestrutura Parse. |
| **`supabase-cloudflare`** | `implementation(project(":bundle:supabase-cloudflare"))` <br> `implementation("br.wgc.omnibackend:bundle-supabase-cloudflare:1.0.0")` | **Par Edge & Dados:** Supabase (PostgreSQL) + Cloudflare Edge (Workers/D1/R2). | Apps com dados relacionais robustos e computação de borda de baixa latência. |

---

## ⚡ Como Usar um Bundle no seu Aplicativo

### 1. Adicionar a Dependência
No `build.gradle.kts` do seu módulo `:app`:

```kotlin
dependencies {
    // Exemplo: Importa o Bundle Híbrido
    implementation(project(":bundle:hybrid"))
}
```

### 2. Inicialização Unificada no `Application.onCreate()`

```kotlin
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Exemplo: Inicializa o Bundle Híbrido
        OmniHybrid.initialize(
            context = this,
            primaryProvider = "firebase",
            secondaryProvider = "supabase"
        )
    }
}
```

### 3. Consumindo os Contratos do `:core`

```kotlin
class UserViewModel(
    private val auth: AuthRepository = OmniHybrid.auth
) : ViewModel() {
    // Seu código só enxerga as interfaces puras do :core!
}
```
