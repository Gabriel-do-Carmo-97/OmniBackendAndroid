# 🌩️ Módulo :backend-cloudflare — Driver Cloudflare

O módulo **`:backend-cloudflare`** integra o Android com soluções de computação e armazenamento em borda da **Cloudflare (Workers, D1 Database, R2 Storage)**.

---

## 🛠️ Recursos Suportados

- **Autenticação:** Autenticação delegada via Cloudflare Workers (`/auth/*`).
- **Banco de Dados:** Cloudflare D1 (Serverless SQL Database) via Worker REST API (`/d1/*`).
- **Armazenamento:** Cloudflare R2 Storage (S3-compatible sem taxas de egress) (`/r2/*`).

---

## ⚡ Exemplo de Inicialização

```kotlin
OmniCloudflare.initialize(
    context = applicationContext,
    accountId = "seu-account-id",
    workerBaseUrl = "https://meu-worker.workers.dev"
)

val auth = OmniCloudflare.auth
val db = OmniCloudflare.database
val storage = OmniCloudflare.storage
```

---

## 🧪 Testes Unitários

```bash
./gradlew :backend-cloudflare:testDebugUnitTest
```
