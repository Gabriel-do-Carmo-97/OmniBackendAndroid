# 🛠️ Módulo :build-logic — Composite Build Convention Plugins

O módulo **`build-logic`** centraliza toda a lógica de build Gradle do **OmniBackend Android** usando **Convention Plugins** do Composite Build.

---

## 🔌 Plugins Disponíveis

- **`omni.android.application`**: Aplica configurações padrão do AGP 9.3.2 para aplicativos Android (`:app`).
- **`omni.android.application.compose`**: Configura Jetpack Compose e Compose Compiler.
- **`omni.android.library`**: Aplica configurações corporativas reutilizáveis para bibliotecas Android (`:core`, `:backend-*`).

---

## 🎯 Benefícios Empresariais

1. **DRY Gradle Scripts**: Evita duplicação de blocos `android { ... }` em múltiplos submódulos.
2. **Type-Safe Accessors**: Usa o catálogo de versões `libs.versions.toml` com tipagem estrita Kotlin.
3. **Manutenibilidade**: Mudança de versão de SDK ou flag de compilação afeta todos os módulos de forma atômica.
