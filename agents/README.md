# 🤖 Sistema Multi-Agente — OmniBackend Android

Este diretório define a equipe de agentes autônomos e especialistas projetados para colaborar no desenvolvimento, manutenção, expansão e garantia de qualidade do **OmniBackend Android**.

---

## 📋 Catálogo de Agentes

### 🧠 1. Domínio & Orquestração
- **[`backend-orchestrator-agent.md`](./backend-orchestrator-agent.md)**: Maestro central responsável pela triagem de demandas, coordenação de fluxos e validação da governança arquitetural.
- **[`core-domain-agent.md`](./core-domain-agent.md)**: Guardião da camada agnóstica (`:core`). Garante que nenhum SDK ou tipo de nuvem entre nos contratos de repositório, modelos ou tratamento de erro.

### ☁️ 2. Drivers de Provedores em Nuvem
- **[`firebase-driver-agent.md`](./firebase-driver-agent.md)**: Especialista no SDK oficial do Firebase (Auth, Firestore, RTDB, Storage, Vertex AI, Telemetria).
- **[`supabase-driver-agent.md`](./supabase-driver-agent.md)**: Especialista no Supabase Kotlin SDK (GoTrue Auth, PostgREST, Realtime, Storage).
- **[`appwrite-driver-agent.md`](./appwrite-driver-agent.md)**: Especialista no Appwrite Android SDK (Account, Databases, Storage, Session cookies).
- **[`back4app-driver-agent.md`](./back4app-driver-agent.md)**: Especialista na plataforma Back4App / Parse SDK (ParseUser, ParseObject, LiveQuery WebSockets, ParseFile).
- **[`aws-amplify-driver-agent.md`](./aws-amplify-driver-agent.md)**: Especialista no AWS Amplify Android SDK (Cognito, S3 Storage, AppSync GraphQL, DynamoDB).
- **[`pocketbase-driver-agent.md`](./pocketbase-driver-agent.md)**: Especialista no PocketBase (RecordAuth, Collections, Server-Sent Events, Files).
- **[`custom-rest-driver-agent.md`](./custom-rest-driver-agent.md)**: Especialista em integração com APIs corporativas proprietárias via Ktor Client / Retrofit com JWT e RFC 7807.
- **[`cloudflare-driver-agent.md`](./cloudflare-driver-agent.md)**: Especialista em soluções de computação e armazenamento em borda da Cloudflare (Workers, R2, D1, Turnstile).

### 🛡️ 3. Qualidade, Segurança, Build & Governança
- **[`security-agent.md`](./security-agent.md)**: Especialista em segurança, criptografia Android Keystore, SSL Pinning e integridade de dispositivo.
- **[`offline-first-agent.md`](./offline-first-agent.md)**: Especialista em arquitetura offline-first, cache local resiliente, filas de mutação e resolução de conflitos.
- **[`usecase-testing-agent.md`](./usecase-testing-agent.md)**: Especialista em testes unitários com MockK, isolamento de corrotinas com Coroutines Test e testes de UseCases.
- **[`code-reviewer-agent.md`](./code-reviewer-agent.md)**: Inspetor de qualidade de código, conformidade com Detekt, validação de Anti-Leak de fornecedores e garantia de 100% KDoc.
- **[`gradle-agent.md`](./gradle-agent.md)**: Especialista no sistema de build Gradle, catálogo `libs.versions.toml`, composite build `build-logic`, Dokka V2 e publicação Maven.
- **[`release-publisher-agent.md`](./release-publisher-agent.md)**: Especialista em SemVer, assinaturas GPG, geração de metadados POM e publicação no Maven Central / GitHub Packages.
- **[`github-agent.md`](./github-agent.md)**: Especialista em CI/CD, Workflows do GitHub Actions, convenções de commits semânticos e diretrizes de PR.
- **[`docs-techwriter-agent.md`](./docs-techwriter-agent.md)**: Especialista em Developer Experience (DX), guias de início rápido, tutoriais de migração e diagramas visuais.

---

## 🔄 Fluxo de Colaboração dos Agentes

```
[Demanda do Usuário]
         │
         ▼
[backend-orchestrator-agent]
   ├── Contrato no :core necessário? ──► [core-domain-agent]
   │                                              │
   ▼                                              ▼
[Driver Especialista] (Firebase / Supabase / Appwrite / Back4App / AWS / PocketBase / REST / Cloudflare)
         │
         ▼
[usecase-testing-agent] ───► Validação de Testes Unitários
         │
         ▼
[code-reviewer-agent]   ───► KDoc 100% & Detekt Compliance
         │
         ▼
[gradle-agent]          ───► Build-logic & Configuração Maven/Dokka
         │
         ▼
[github-agent]          ───► CI/CD & Commit Semântico
```

---

## ⚖️ Princípios Invioláveis do Sistema

1. **Pureza do `:core`**: O módulo central jamais conhecerá a existência de SDKs de fornecedores.
2. **Anti-Leak Estrito**: Os drivers são cascas descartáveis; todo tipo proprietário deve ser convertido para `OmniUser` e os erros para `AppError`.
3. **100% KDoc**: Toda função, classe ou parâmetro exposto deve ser documentado.
4. **Zero Regressão**: Toda modificação deve manter 100% de testes unitários aprovados (`./gradlew testDebugUnitTest`).
