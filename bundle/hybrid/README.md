# 🛡️ Módulo `:bundle:hybrid` — Redundância Híbrida & Failover em Tempo Real

O módulo **`:bundle:hybrid`** é focado em **Alta Disponibilidade, Redundância Multi-Nuvem e Failover em Tempo Real** agrupando os principais provedores de mercado (`firebase`, `supabase`, `appwrite`).

---

## 🛠️ O que está incluído?

- `:core`
- `:backend:firebase`
- `:backend:supabase`
- `:backend:appwrite`

---

## 📥 Como Importar

```kotlin
dependencies {
    implementation(project(":bundle:hybrid"))
}
```

---

## 🎯 Quando Usar?

- **Aplicações Críticas (Zero Downtime):** Bancos, fintechs, e-commerce, saúde e transporte.
- **Failover Automático (Circuit Breaker):** O app tenta autenticar ou gravar no Firebase; se houver instabilidade ou timeout na rede, o `HybridAuthRepository` altera de forma transparente para o Supabase ou Appwrite.
- **Double/Triple Writes:** Gravação simultânea redundante em múltiplos provedores para garantir prevenção total contra perda de dados (*Disaster Recovery*).
