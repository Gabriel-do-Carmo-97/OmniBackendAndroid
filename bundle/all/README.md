# 📦 Módulo `:bundle:all` — Suíte Completa OmniBackend

O módulo **`:bundle:all`** inclui **TODOS os 8 drivers de backend** do OmniBackend Android em um único pacote.

---

## 🛠️ O que está incluído?

- `:core` (Contratos, Resiliência Offline, Segurança Keystore)
- `:backend:firebase` (Google Firebase)
- `:backend:supabase` (Supabase)
- `:backend:appwrite` (Appwrite)
- `:backend:pocketbase` (PocketBase)
- `:backend:back4app` (Back4App / Parse)
- `:backend:amplify` (AWS Amplify / Cognito / S3)
- `:backend:cloudflare` (Cloudflare Workers / D1 / R2)
- `:backend:rest` (Custom REST API)

---

## 📥 Como Importar

No `build.gradle.kts` do seu app:

```kotlin
dependencies {
    implementation(project(":bundle:all"))
    // Ou via publicação Maven:
    // implementation("br.wgc.omnibackend:bundle-all:1.0.0")
}
```

---

## 🎯 Quando Usar?

- **Prototipagem Ágil & PoCs:** Quando você precisa testar e comparar múltiplos provedores de nuvem no mesmo projeto.
- **Super-Apps Corporativos:** Aplicações com dezenas de módulos em que diferentes áreas utilizam fornecedores distintos.
- **Ambientes de Testes Integrados:** Para simulação e validação completa de suítes de testes.
