# Changelog

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [Unreleased]

### Adicionado
- Diretrizes de governança e arquitetura em `ARCHITECTURE.md` e `INTEGRATION_GUIDE.md`.
- Suporte a verificação de código estático via SonarQube e auditoria OWASP Dependency-Check.
- Otimizações de build no `gradle.properties` (`parallel`, `caching`, `nonTransitiveRClass`).
- Relatório consolidado de cobertura `jacocoRootReport`.
- Testes de fitness arquitetural (`ArchitectureFitnessTest`) e suíte de testes do `bundle:all`.
- Pipeline de segurança com CodeQL SAST, scan de segredos Gitleaks e auditoria de licenças.

---

## [1.0.0] - 2026-09-12

### Adicionado
- Módulo agnóstico `:core` com contratos para `AuthRepository`, `FirestoreRepository`, `StorageRepository`, `AnalyticsRepository` e `RealtimeRepository`.
- 8 drivers de backend especializados:
  - `:backend:firebase` (Google Firebase BoM)
  - `:backend:supabase` (Supabase Kotlin SDK)
  - `:backend:appwrite` (Appwrite SDK)
  - `:backend:pocketbase` (PocketBase client)
  - `:backend:back4app` (Parse Platform SDK)
  - `:backend:amplify` (AWS Amplify Android)
  - `:backend:cloudflare` (Cloudflare Workers/D1/R2)
  - `:backend:rest` (Custom REST API RFC 7807)
- 11 bundles agregadores corporativos com módulos Dagger/Hilt prontos para consumo:
  - `bundle:all`
  - `bundle:hybrid`
  - `bundle:self-hosted`
  - `bundle:cloud-native`
  - `bundle:enterprise-hybrid`
  - `bundle:edge-serverless`
  - `bundle:baas-classic`
  - `bundle:firebase-supabase`
  - `bundle:firebase-amplify`
  - `bundle:firebase-back4app`
  - `bundle:supabase-cloudflare`
- Módulo de testes isolados `:testing` com fakes em memória.
- Aplicativo de demonstração `:app` com playground interativo e catálogo de bundles.
- Esteira CI/CD hipergranular baseada em GitHub Actions com publicação em GitHub Packages.
