# 🏛️ Módulo :core — Núcleo Agnóstico de Domínio

O módulo **`:core`** é o coração arquitetural do **OmniBackend Android**. Ele define contratos, modelos de domínio e hierarquias de erro puras e totalmente agnósticas a fornecedores de nuvem.

---

## 📐 Princípio Arquitetural Inviolável

> **Zero Dependências de Terceiros:**
> O módulo `:core` **nunca** dependerá de SDKs de nuvem (Firebase, Supabase, Appwrite, AWS, etc.). Ele só utiliza Kotlin Standard Library, Coroutines e bibliotecas base do Android.

---

## 🧩 Componentes Principais

### 1. Interfaces de Repositório
- `AuthRepository`: Login, cadastro, sessão, logout, redefinição de senha e alteração de perfil.
- `FirestoreRepository`: Persistência NoSQL/Documentos com suporte a filtros, atualizações em tempo real (`Flow`) e CRUD.
- `StorageRepository`: Uploads diretos/suspensos de bytes/URIs, geração de URLs públicas de download e exclusão.
- `AnalyticsRepository` & `RemoteConfigRepository`: Métricas e sinalizadores de recurso remotos.
- `RealtimeDatabaseRepository`, `GeolocationRepository`, `MessageRepository`, `PresenceRepository`: Comunicação em tempo real, presença e geolocalização.

### 2. Modelos de Domínio
- `OmniUser`: Representação agnóstica de usuário (`uid`, `email`, `displayName`, `photoUrl`, `isEmailVerified`, `isAnonymous`).
- `DataResult<T>`: Result wrapper selado (`DataResult.Success`, `DataResult.Failure`).
- `AppError`: Hierarquia selada de erros corporativos (`AppError.Auth.*`, `AppError.Firestore.*`, `AppError.Storage.*`, `AppError.Generic.*`).

---

## 🧪 Testes Unitários

Para rodar os testes unitários do `:core`:

```bash
./gradlew :core:testDebugUnitTest
```
