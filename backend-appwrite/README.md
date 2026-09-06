# ⚙️ Módulo :backend-appwrite — Driver Appwrite

O módulo **`:backend-appwrite`** implementa os contratos do `:core` consumindo o **Appwrite Android SDK (v24.1.1)**.

---

## 🛠️ Recursos Suportados

- **Autenticação:** Appwrite Account API (`email/senha`, sessões anônimas, recuperação de senha).
- **Banco de Dados:** Appwrite Databases API & Realtime WebSockets (`createDocument`, `getDocument`, `listDocuments`, `subscribe`).
- **Armazenamento:** Appwrite Storage API (`createFile`, `deleteFile`, URLs públicas de visualização).
- **Mapeamento Selado:** Mapeamento de `AppwriteException` para `AppError`.

---

## ⚡ Exemplo de Inicialização

```kotlin
OmniAppwrite.initialize(
    context = applicationContext,
    endpoint = "https://cloud.appwrite.io/v1",
    projectId = "seu-project-id",
    databaseId = "seu-database-id",
    defaultBucketId = "seu-bucket-id"
)

val auth = OmniAppwrite.auth
val db = OmniAppwrite.database
val storage = OmniAppwrite.storage
```

---

## 🧪 Testes Unitários

```bash
./gradlew :backend-appwrite:testDebugUnitTest
```
