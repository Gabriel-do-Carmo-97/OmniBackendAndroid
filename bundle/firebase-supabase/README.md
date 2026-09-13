# ⚡ Módulo `:bundle:firebase-supabase` — GCP Firebase + Supabase PostgreSQL

O módulo **`:bundle:firebase-supabase`** é um **Bundle Pareado Especializado (Pair Bundle)** pré-configurado para combinar os dois ecossistemas de Backend-as-a-Service (BaaS) modernos mais populares do mercado: **Google Firebase** e **Supabase**.

---

## 🎯 Por que este Bundle foi criado?

Nos bundles macro como `:bundle:hybrid`, são embutidos 3 ou mais provedores (Firebase, Supabase, Appwrite). No entanto, muitas equipes corporativas adotam uma estratégia focada estritamente em dois pilares:
1. **Google Firebase (GCP):** Referência em tempo real, Cloud Messaging (FCM), Crashlytics e Analytics.
2. **Supabase (PostgreSQL):** Poder de consultas SQL relacionais complexas, RLS (Row Level Security), triggers e banco relacional aberto.

Este módulo isola **apenas** as dependências necessárias para esses dois provedores, gerando um binário (AAR) menor e livre de bibliotecas extras.

---

## 🛠️ O que está incluído?

- `:core` (Contratos agnósticos de domínio)
- `:bundle:hybrid` (Motor de failover ativo-passivo com Circuit Breaker)
- `:backend:firebase` (Driver Google Firebase)
- `:backend:supabase` (Driver Supabase Kotlin / PostgREST)

---

## 📥 Como Importar

No `build.gradle.kts` do seu módulo `:app`:

```kotlin
dependencies {
    implementation(project(":bundle:firebase-supabase"))
    // Ou via GitHub Packages / Maven:
    // implementation("br.wgc.omnibackend:bundle-firebase-supabase:1.0.0")
}
```

---

## 🎯 Quando Usar?

- **Failover Ativo-Passivo (GCP -> Supabase):** O app opera prioritariamente com Firebase; se o Firestore ou Auth oscilarem, o motor do `OmniFirebaseSupabase` altera de forma transparente para o Supabase PostgreSQL.
- **Migração Gradual (NoSQL para Relacional):** Ideal para sistemas em transição do Firestore para PostgreSQL sem precisar refatorar ViewModels ou UseCases.
- **Divisão Estratégica de Dados:** Usar autenticação e push notifications do Firebase e tabelas relacionais com dados sensíveis no Supabase.

---

## 💻 Exemplo Prático de Uso

```kotlin
import br.wgc.omnibackend.bundle.firebasesupabase.OmniFirebaseSupabase
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository

// 1. Repositórios Híbridos com Failover Automático
val authRepository: AuthRepository = OmniFirebaseSupabase.createAuth()
val databaseRepository: FirestoreRepository = OmniFirebaseSupabase.createDatabase()

// 2. Acesso Direto aos Provedores Quando Necessário
val firebaseAuth = OmniFirebaseSupabase.firebase.auth
val supabaseDb = OmniFirebaseSupabase.supabase.database
```
