# ☁️ Módulo :backend-amplify — Driver AWS Amplify

O módulo **`:backend-amplify`** implementa os contratos do `:core` utilizando serviços da **AWS (Cognito, DynamoDB/AppSync, S3 Storage)**.

---

## 🛠️ Recursos Suportados

- **Autenticação:** AWS Cognito User Pools (signUp, signIn, gerenciamento de perfil e credenciais).
- **Banco de Dados:** AWS AppSync / DynamoDB (operações de dados e sincronização).
- **Armazenamento:** AWS S3 Storage (upload, download e expiração de URLs).

---

## ⚡ Exemplo de Inicialização

```kotlin
OmniAmplify.initialize(applicationContext)

val auth = OmniAmplify.auth
val db = OmniAmplify.database
val storage = OmniAmplify.storage
```

---

## 🧪 Testes Unitários

```bash
./gradlew :backend-amplify:testDebugUnitTest
```
