# 🤖 Suíte de Agentes Especialistas — OmniBackend Android

Esta pasta contém as especificações, prompts, regras invioláveis e protocolos de trabalho dos **Agentes Especialistas de IA** do framework **OmniBackend Android**.

---

## 🏛️ Filosofia Operacional

O ecossistema adota a **Segregação Estrita de Responsabilidades** (Single Responsibility Principle) e **Arquitetura Hexagonal**:
- O **Orquestrador** nunca gera código diretamente; sua função é coordenar fluxos cross-module e acionar os especialistas.
- O **Core Domain Agent** é o guardião da camada de domínio pura (`:core`), garantindo zero acoplamento com fornecedores de nuvem.
- Cada provedor de nuvem tem seu próprio **Driver Agent Especialista** (`firebase`, `supabase`, `appwrite`), preservando o contexto nativo de cada SDK.
- O **Code Reviewer** é um auditor independente: nunca altera o código, reportando falhas técnicas com linha e severidade para o agente responsável corrigir.

---

## 👥 Mapa de Agentes

| Agente | Arquivo | Responsabilidade Principal |
| :--- | :--- | :--- |
| **Orquestrador Geral** | [`omni-backend-orchestrator.md`](./omni-backend-orchestrator.md) | Coordenação cross-module, planejamento arquitetural e delegação. |
| **Domínio Agnóstico** | [`core-domain-agent.md`](./core-domain-agent.md) | Contratos de repositório, `OmniUser`, `DataResult` e hierarquia `AppError`. |
| **Driver Firebase** | [`firebase-driver-agent.md`](./firebase-driver-agent.md) | Implementação Firebase (BoM 33.9.0), App Check, Telemetria e Mappers. |
| **Driver Supabase** | [`supabase-driver-agent.md`](./supabase-driver-agent.md) | Implementação Supabase Kotlin SDK (PostgREST, GoTrue, Realtime, Storage). |
| **Driver Appwrite** | [`appwrite-driver-agent.md`](./appwrite-driver-agent.md) | Implementação Appwrite Android SDK (Account, Databases, Storage). |
| **Casos de Uso & Testes** | [`usecase-testing-agent.md`](./usecase-testing-agent.md) | UseCases de negócio, Fakes em memória e testes unitários com MockK. |
| **Code Review & Qualidade** | [`code-reviewer-agent.md`](./code-reviewer-agent.md) | Checklists de Anti-Leak, OWASP Mobile, Concorrência e Hexagonal. |
| **Engenheiro de Build** | [`gradle-agent.md`](./gradle-agent.md) | `build-logic`, convention plugins, `libs.versions.toml` e tarefas Gradle. |
| **Workflow & CI/CD** | [`github-agent.md`](./github-agent.md) | PRs semânticos, automação de GitHub Actions e publicação de AARs. |
