# Diretrizes de Contribuição para o OmniBackendAndroid

Agradecemos o seu interesse em contribuir com o **OmniBackendAndroid**! Este documento orienta o desenvolvimento colaborativo mantendo o padrão corporativo de arquitetura, qualidade e documentação.

---

## 🏛️ Princípios de Arquitetura

O ecossistema é projetado com base em **Clean Architecture** e **Arquitetura Hexagonal (Portas e Adaptadores)**:

1. **Módulo `:core` (Puro e Agnóstico)**:
   - Contém apenas contratos (interfaces de repositórios, provedores de telemetria), entidades de domínio imutáveis (`OmniUser`) e o tipo funcional `DataResult<T>` com a hierarquia selada `AppError`.
   - **Regra de Ouro**: O módulo `:core` **nunca** deve conter dependências de fornecedores em nuvem (sem Firebase, sem Supabase, sem Appwrite).
2. **Módulos Adaptadores (`:backend-firebase`, `:backend-supabase`, etc.)**:
   - Devem implementar **exclusivamente** os contratos definidos no `:core`.
   - Todas as exceções do provedor devem ser capturadas e mapeadas para subclasses de `AppError`.
   - Tipos nativos dos SDKs (como `FirebaseUser`) **jamais** devem vazar para a assinatura pública de repositórios ou casos de uso; converta sempre para os modelos do `:core`.
3. **Módulo `build-logic` (Plugins de Convenção)**:
   - Gerencia de forma centralizada as configurações do Android Gradle Plugin, Kotlin DSL, compilação (JVM 11, compileSdk 37), Dokka e publicação Maven.

---

## 📝 Padrões de Código e Documentação (KDoc)

- **KDoc 100% Obrigatório**: Toda classe pública, interface, função, parâmetro (`@param`), retorno (`@return`) e possível erro (`@throws`) deve possuir documentação detalhada em português ou inglês com formatação KDoc oficial.
- **Análise Estática (Detekt)**: Todo código deve passar sem alertas pelas regras de qualidade configuradas em `config/detekt/detekt.yml`. Execute `./gradlew detekt` localmente antes de submeter um PR.
- **Testes Unitários**: Toda nova funcionalidade ou correção de bug deve ser acompanhada por testes unitários com MockK (`./gradlew testDebugUnitTest`).

---

## 🌿 Fluxo de Git e Commits

- **Conventional Commits**: Siga o padrão padronizado:
  - `feat(modulo): nova funcionalidade`
  - `fix(modulo): correção de comportamento`
  - `refactor(modulo): melhoria interna sem alteração de comportamento externo`
  - `docs: documentação ou KDoc`
  - `test: criação ou ajuste de testes`
  - `build: mudanças de build-logic, gradle ou dependabot`
  - `ci: fluxos do GitHub Actions`
- **Branches**: Crie branches a partir da `master` com prefixos semânticos (ex: `feature/suporte-appwrite`, `fix/auth-reauthentication`).

---

## 🚀 Como Submeter um Pull Request (PR)

1. Crie uma branch para a sua modificação.
2. Certifique-se de que a suite de validação passa com 100% de sucesso:
   ```bash
   ./gradlew detekt
   ./gradlew testDebugUnitTest
   ./gradlew assembleRelease
   ```
3. Abra o Pull Request descrevendo claramente o objetivo da alteração e os testes realizados.
