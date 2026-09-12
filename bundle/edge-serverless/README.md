# 🌩️ Módulo `:bundle:edge-serverless` — Computação em Borda & Ultra-Baixa Latência

O módulo **`:bundle:edge-serverless`** é focado em **respostas em milissegundos e computação serverless leve em borda** (`cloudflare`, `pocketbase`, `rest`).

---

## 🛠️ O que está incluído?

- `:core`
- `:backend:cloudflare`
- `:backend:pocketbase`
- `:backend:rest`

---

## 📥 Como Importar

```kotlin
dependencies {
    implementation(project(":bundle:edge-serverless"))
}
```

---

## 🎯 Quando Usar?

- **Ultra-Baixa Latência (Edge Computing):** Requisições processadas nos pontos de presença (PoP) mais próximos do usuário via Cloudflare Workers, D1 e R2.
- **Aplicações IoT, Dispositivos Embarcados & Microsserviços Leves:** Ambientes que exigem baixo consumo de memória, conexões ultrarrápidas ou banco de dados SQLite embarcado (PocketBase).
