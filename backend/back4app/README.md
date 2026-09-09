# 📦 Módulo :backend-back4app — Driver Back4App / Parse Platform

O módulo **`:backend-back4app`** é o driver de integração para a plataforma **Back4App / Parse Server** utilizando o **Parse Android SDK (v4.4.0)**.

---

## 🛠️ Recursos Suportados

- **Autenticação:** `ParseUser` (`signUp`, `logIn`, `requestPasswordReset`, gerenciamento de sessão).
- **Banco de Dados:** `ParseObject` e `ParseQuery` (`save`, `delete`, consultas com filtros e ordenação).
- **Armazenamento:** `ParseFile` (upload de arquivos e URLs remotas).

---

## ⚡ Exemplo de Inicialização

```kotlin
OmniBack4App.initialize(
    context = applicationContext,
    appId = "seu-back4app-app-id",
    clientKey = "sua-client-key",
    serverUrl = "https://parseapi.back4app.com"
)

val auth = OmniBack4App.auth
val db = OmniBack4App.database
val storage = OmniBack4App.storage
```

---

## 🧪 Testes Unitários

```bash
./gradlew :backend-back4app:testDebugUnitTest
```
