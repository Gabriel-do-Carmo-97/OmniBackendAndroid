# 🗄️ Módulo :backend-pocketbase — Driver PocketBase

O módulo **`:backend-pocketbase`** é a implementação concreta dos contratos do `:core` consumindo servidores **PocketBase (Go / SQLite / Realtime)**.

---

## 🛠️ Recursos Suportados

- **Autenticação:** `/api/collections/users/auth-with-password`, criação de conta e renovação de token.
- **Banco de Dados:** `/api/collections/{collection}/records` (CRUD, busca com filtros, ordenação).
- **Armazenamento:** `/api/files/{collection}/{recordId}/{file}` (serviço e visualização de arquivos).

---

## ⚡ Exemplo de Inicialização

```kotlin
OmniPocketBase.initialize(
    context = applicationContext,
    baseUrl = "https://seu-pocketbase.app"
)

val auth = OmniPocketBase.auth
val db = OmniPocketBase.database
val storage = OmniPocketBase.storage
```

---

## 🧪 Testes Unitários

```bash
./gradlew :backend-pocketbase:testDebugUnitTest
```
