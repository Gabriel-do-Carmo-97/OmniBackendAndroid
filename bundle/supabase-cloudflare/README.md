# 🌐 Módulo `:bundle:supabase-cloudflare` — Supabase PostgreSQL + Cloudflare Edge

O módulo **`:bundle:supabase-cloudflare`** é um **Bundle Pareado Especializado (Pair Bundle)** que combina o banco de dados relacional **Supabase (PostgreSQL)** com a infraestrutura global de computação em borda **Cloudflare Edge (Workers / D1 / R2)**.

---

## 🎯 Por que este Bundle foi criado?

Esta é a combinação arquitetural mais moderna e recomendada pela comunidade atual de engenharia de software para aplicações com alta densidade de tráfego e latência ultra-baixa:
1. **Supabase:** Gerencia a fonte da verdade relacional (PostgreSQL), regras de segurança (RLS) e autenticação GoTrue.
2. **Cloudflare Edge:** Distribui dados e arquivos em mais de 300 data centers mundiais com latência inferior a 30ms via Workers, D1 (SQLite distribuído) e R2 (Object Storage com zero taxa de egress).

---

## 🛠️ O que está incluído?

- `:core` (Contratos agnósticos)
- `:bundle:hybrid` (Motor de failover e integração híbrida)
- `:backend:supabase` (Driver Supabase Kotlin / PostgREST)
- `:backend:cloudflare` (Driver Cloudflare Edge / Workers / R2)

---

## 📥 Como Importar

No `build.gradle.kts` do seu módulo `:app`:

```kotlin
dependencies {
    implementation(project(":bundle:supabase-cloudflare"))
    // Ou via GitHub Packages / Maven:
    // implementation("br.wgc.omnibackend:bundle-supabase-cloudflare:1.0.0")
}
```

---

## 🎯 Quando Usar?

- **Aplicações Globais com Edge Caching:** Casos onde a aplicação precisa ler dados com latência de milissegundos da borda (Cloudflare) e persistir dados transacionais complexos no PostgreSQL (Supabase).
- **Economia Máxima de Armazenamento (Zero Egress):** Upload e entrega de mídia pesada através do Cloudflare R2 com controle de metadados e permissões no Supabase.
- **Aplicações sem Dependência de Big Techs:** Arquitetura 100% livre do ecossistema Google (Firebase) ou Amazon (AWS).

---

## 💻 Exemplo Prático de Uso

```kotlin
import br.wgc.omnibackend.bundle.supabasecloudflare.OmniSupabaseCloudflare
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository

// 1. Repositórios Pareados
val auth: AuthRepository = OmniSupabaseCloudflare.createAuth() // Supabase GoTrue -> Cloudflare Access
val database: FirestoreRepository = OmniSupabaseCloudflare.createDatabase() // Supabase PostgREST -> Cloudflare D1/KV
val storage: StorageRepository = OmniSupabaseCloudflare.createStorage() // Supabase Storage -> Cloudflare R2

// 2. Acesso Direto aos Provedores
val supabaseDb = OmniSupabaseCloudflare.supabase.database
val cloudflareEdge = OmniSupabaseCloudflare.cloudflare.storage
```
