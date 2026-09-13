# 🛡️ Módulo `:bundle:firebase-back4app` — Google Firebase + Back4App (Parse Platform)

O módulo **`:bundle:firebase-back4app`** é um **Bundle Pareado Especializado (Pair Bundle)** desenhado para unir a escala do **Google Firebase** com a flexibilidade da arquitetura **Parse Platform (Back4App)**.

---

## 🎯 Por que este Bundle foi criado?

O Back4App é amplamente utilizado por empresas que apreciam o modelo BaaS tradicional com banco de dados NoSQL/Parse, APIs GraphQL/REST automáticas e Cloud Code em Node.js.

Este bundle permite que aplicações mantenham o Firebase como ecossistema principal enquanto mantêm uma infraestrutura de contingência rápida e econômica no Back4App.

---

## 🛠️ O que está incluído?

- `:core` (Contratos agnósticos)
- `:bundle:hybrid` (Motor de failover ativo-passivo com Circuit Breaker)
- `:backend:firebase` (Driver Google Firebase)
- `:backend:back4app` (Driver Back4App / Parse SDK)

---

## 📥 Como Importar

No `build.gradle.kts` do seu módulo `:app`:

```kotlin
dependencies {
    implementation(project(":bundle:firebase-back4app"))
    // Ou via GitHub Packages / Maven:
    // implementation("br.wgc.omnibackend:bundle-firebase-back4app:1.0.0")
}
```

---

## 🎯 Quando Usar?

- **Contingência Econômica BaaS:** Para equipes que desejam contingência contra quedas ou limites de cota do Firebase usando um BaaS tradicional confiável.
- **Operações em Países com Restrições:** Cenários onde determinados serviços do Google enfrentam restrições geográficas ou bloqueios regulatórios temporários.
- **Aceleração de Entrega:** Aproveitar schemas relacionais flexíveis do Parse Object com a telemetria do Firebase.

---

## 💻 Exemplo Prático de Uso

```kotlin
import br.wgc.omnibackend.bundle.firebaseback4app.OmniFirebaseBack4App
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository

// Failover automático: Firebase (Primário) -> Back4App Parse (Secundário)
val auth: AuthRepository = OmniFirebaseBack4App.createAuth()
val database: FirestoreRepository = OmniFirebaseBack4App.createDatabase()
```
