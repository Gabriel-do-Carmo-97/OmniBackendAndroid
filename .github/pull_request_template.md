## 📋 Descrição do Pull Request

<!-- Descreva de forma clara e concisa o objetivo deste PR -->

### Tipo de Mudança:
- [ ] 🚀 `feat`: Nova funcionalidade
- [ ] 🐛 `fix`: Correção de bug
- [ ] 🔨 `refactor`: Refatoração de código (sem alteração de comportamento público)
- [ ] ⚡ `perf`: Melhoria de performance
- [ ] 🧪 `test`: Adição ou correção de testes unitários
- [ ] 📚 `docs`: Alteração em documentação ou KDoc
- [ ] 🔧 `chore`: Tarefas de build, CI/CD ou dependências

---

## 🏛️ Módulos Afetados:
- [ ] `:core` (Contratos agnósticos, Failover, Modelos)
- [ ] `:backend:firebase`
- [ ] `:backend:supabase`
- [ ] `:backend:appwrite`
- [ ] `:backend:pocketbase`
- [ ] `:backend:back4app`
- [ ] `:backend:amplify`
- [ ] `:backend:rest`
- [ ] `:backend:cloudflare`
- [ ] `:bundle:hybrid`
- [ ] `:bundle:self-hosted`
- [ ] `:bundle:cloud-native`
- [ ] `:bundle:all`

---

## ✅ Checklist de Qualidade e Governança:
- [ ] O título do PR segue o padrão **Conventional Commits** (`feat:`, `fix:`, `refactor:`, etc.).
- [ ] Todo o código público contém **100% de documentação KDoc**.
- [ ] As classes internas ou de SDKs específicos estão marcadas como `internal` (**Zero SDK Leakage**).
- [ ] O comando `./gradlew detekt` foi executado localmente sem violações.
- [ ] O comando `./gradlew testDebugUnitTest` passou com 100% de sucesso.
- [ ] Não há quebra não autorizada de compatibilidade binária ou contratos públicos.
