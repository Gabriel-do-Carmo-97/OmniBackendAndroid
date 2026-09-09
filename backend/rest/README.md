# 🌐 Módulo :backend-rest — Driver Custom REST API

O módulo **`:backend-rest`** é um driver genérico de integração com APIs REST corporativas proprietárias e microserviços.

---

## 🛠️ Recursos Suportados

- **Autenticação:** JWT Auth Endpoints (`/auth/login`, `/auth/register`, `/auth/reset-password`).
- **Banco de Dados:** Endpoints REST corporativos (`/api/v1/{collection}/*`).
- **Armazenamento:** Endpoints REST de arquivos (`/api/v1/storage/*`).
- **Tratamento de Erros:** Suporte a RFC 7807 (Problem Details) e códigos HTTP.

---

## ⚡ Exemplo de Inicialização

```kotlin
OmniRestBackend.initialize(
    context = applicationContext,
    baseUrl = "https://api.suaempresa.com"
)

val auth = OmniRestBackend.auth
val db = OmniRestBackend.database
val storage = OmniRestBackend.storage
```

---

## 🧪 Testes Unitários

```bash
./gradlew :backend-rest:testDebugUnitTest
```
