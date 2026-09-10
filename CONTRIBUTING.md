# Guia de Contribuição — OmniBackendAndroid 🚀

Obrigado pelo seu interesse em contribuir com o **OmniBackendAndroid**!
Este repositório segue rigorosos padrões de engenharia de software móvel, Clean Architecture e convenções multi-módulo corporativas.

---

## 🏛️ Princípios Arquiteturais Obrigatórios

1. **Zero SDK Leakage (Isolamento Estrito):** Nenhuma classe ou modelo específico de SDKs de terceiros (Firebase, Supabase, Appwrite, PocketBase, etc.) deve vazar além de seu respectivo módulo `:backend:<nome>`. Camadas superiores de aplicação enxergam única e exclusivamente as entidades do `:core` (`OmniUser`, `DataResult`, `AppError`).
2. **Abstração por Interfaces:** Toda funcionalidade deve ser declarada como contrato de interface no `:core` (`AuthRepository`, `FirestoreRepository`, `StorageRepository`, etc.) antes de receber implementações concretas.
3. **KDoc Exaustivo (100%):** Qualquer classe, interface, propriedade ou método público DEVE conter documentação KDoc em português, detalhando propósito, `@param`, `@return` e `@throws` quando aplicável.
4. **Resiliência e Failover:** Recursos corporativos devem ser compatíveis com a orquestração híbrida de `:bundle:hybrid`.

---

## 🛠️ Padrão de Commits (Conventional Commits)

Todas as mensagens de commit e títulos de Pull Requests **DEVEM** seguir a especificação de [Conventional Commits](https://www.conventionalcommits.org/):

* `feat(<modulo>):` Nova funcionalidade (ex: `feat(hybrid): add active-active quorum sync`)
* `fix(<modulo>):` Correção de bug (ex: `fix(appwrite): prevent token refresh crash`)
* `refactor(<modulo>):` Refatoração sem alteração de comportamento externo
* `test(<modulo>):` Adição ou alteração de testes unitários com MockK
* `docs(<modulo>):` Alterações exclusivas na documentação ou KDocs
* `chore(<modulo>):` Atualização de dependências ou automação de build

---

## 🧪 Validação Local Antes de Abrir PR

Antes de enviar seus commits para o repositório remoto, certifique-se de que todas as validações passam localmente:

```bash
# 1. Análise estática com Detekt
./gradlew detekt

# 2. Executar suíte completa de testes unitários
./gradlew testDebugUnitTest

# 3. Compilar AARs de release
./gradlew assembleRelease
```

---

## 🔀 Fluxo de Branches e Pull Requests

1. Crie uma branch a partir de `master`:
   ```bash
   git checkout -b feat/<nome-da-feature>
   # ou
   git checkout -b fix/<nome-do-bug>
   ```
2. Realize commits atômicos com mensagens padronizadas.
3. Envie a branch para o remoto e abra um Pull Request preenchendo o template.
