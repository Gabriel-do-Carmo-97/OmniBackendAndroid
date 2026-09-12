# 🏢 Módulo `:bundle:enterprise-hybrid` — Nuvem Híbrida & Sistemas Legados

O módulo **`:bundle:enterprise-hybrid`** conecta o app Android com **sistemas corporativos legados, APIs REST internas e nuvens corporativas** (`amplify`, `cloudflare`, `rest`, `firebase`).

---

## 🛠️ O que está incluído?

- `:core`
- `:backend:amplify`
- `:backend:cloudflare`
- `:backend:rest`
- `:backend:firebase`

---

## 📥 Como Importar

```kotlin
dependencies {
    implementation(project(":bundle:enterprise-hybrid"))
}
```

---

## 🎯 Quando Usar?

- **Empresas Consolidadas / Legado:** Corporações que possuem APIs REST/SOAP internas legadas, mas desejam integrar novos serviços na AWS (Cognito/S3), Cloudflare ou Firebase.
- **Transição de Arquitetura Monolítica para Microsserviços:** Permite migrar endpoints gradualmente sem reescrever o aplicativo.
