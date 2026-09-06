# ⚡ Módulo :backend-supabase — Driver Supabase

O módulo **`:backend-supabase`** é a implementação concreta dos contratos do `:core` utilizando o **Supabase Kotlin SDK (v3.1.3)**.

---

## 🛠️ Recursos Suportados

- **Autenticação:** GoTrue Auth (`Email/Password`, sessões JWT reativas via `authState`).
- **Banco de Dados:** PostgREST (`select`, `insert`, `update`, `delete`, `decodeList<T>`).
- **Armazenamento:** Supabase Storage (`buckets`, upload de arquivos, public URLs).
- **Mapeamento Selado:** Conversão automática de `RestException` e `AuthException` para `AppError`.

---

## ⚡ Exemplo de Inicialização

```kotlin
OmniSupabase.initialize(
    context = applicationContext,
    url = "https://sua-empresa.supabase.co",
    anonKey = "sua-anon-key"
)

val auth = OmniSupabase.auth
val db = OmniSupabase.database
val storage = OmniSupabase.storage
```

---

## 🧪 Testes Unitários

```bash
./gradlew :backend-supabase:testDebugUnitTest
```
