# 🏠 Módulo `:bundle:self-hosted` — Infraestrutura Auto-Hospedada & Open Source

O módulo **`:bundle:self-hosted`** agrupa os drivers para plataformas de código aberto e **auto-hospedadas / On-Premise** (`supabase`, `appwrite`, `pocketbase`, `back4app`).

---

## 🛠️ O que está incluído?

- `:core`
- `:backend:supabase`
- `:backend:appwrite`
- `:backend:pocketbase`
- `:backend:back4app`

---

## 📥 Como Importar

```kotlin
dependencies {
    implementation(project(":bundle:self-hosted"))
}
```

---

## 🎯 Quando Usar?

- **Privacidade & Governança Estrita (LGPD / GDPR):** Quando os dados do usuário não podem sair da infraestrutura ou servidores privados da empresa.
- **Redes Privadas / VPNs Corporativas:** Para instalar e consumir servidores no seu próprio Kubernetes, Docker ou data center On-Premise.
- **Prevenção contra Aprisionamento (Vendor Lock-in):** Liberdade total para mudar de servidor sem dependência de Big Techs.
