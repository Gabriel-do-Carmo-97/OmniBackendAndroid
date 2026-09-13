# ☁️ Módulo `:bundle:firebase-amplify` — Redundância Multi-Cloud Corporativa (GCP + AWS)

O módulo **`:bundle:firebase-amplify`** é um **Bundle Pareado Especializado (Pair Bundle)** que conecta diretamente os dois maiores gigantes da computação em nuvem: **Google Cloud Platform (Firebase)** e **Amazon Web Services (Amplify)**.

---

## 🎯 Por que este Bundle foi criado?

Muitas empresas de grande porte e órgãos regulamentados possuem políticas rígidas de **Continuidade de Negócios (BCP)** e **Disaster Recovery (DR)** que proíbem a dependência de um único fornecedor de nuvem (*Cloud Lock-in*).

Com o `:bundle:firebase-amplify`, o aplicativo possui os SDKs de ambas as nuvens prontos para failover sem precisar carregar bibliotecas de auto-hospedagem ou edge computing.

---

## 🛠️ O que está incluído?

- `:core` (Contratos agnósticos)
- `:bundle:hybrid` (Motor de failover ativo-passivo com Circuit Breaker)
- `:backend:firebase` (Driver Google Firebase)
- `:backend:amplify` (Driver AWS Amplify — Cognito, DynamoDB, S3)

---

## 📥 Como Importar

No `build.gradle.kts` do seu módulo `:app`:

```kotlin
dependencies {
    implementation(project(":bundle:firebase-amplify"))
    // Ou via GitHub Packages / Maven:
    // implementation("br.wgc.omnibackend:bundle-firebase-amplify:1.0.0")
}
```

---

## 🎯 Quando Usar?

- **Disaster Recovery Multi-Cloud (GCP <-> AWS):** Garantir que, caso uma região ou serviço global do GCP/AWS sofra um apagão, o aplicativo continue funcionando no outro provedor.
- **Ambientes Corporativos Híbridos:** Empresas que já utilizam contratos corporativos com AWS (S3, DynamoDB, Cognito) e querem o ecossistema móvel do Firebase (FCM, Crashlytics, Analytics).
- **Failover Transparente de Armazenamento:** Gravação primária no Firebase Storage e contingência em bucket Amazon S3.

---

## 💻 Exemplo Prático de Uso

```kotlin
import br.wgc.omnibackend.bundle.firebaseamplify.OmniFirebaseAmplify
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository

// 1. Repositórios com Failover Multi-Cloud Automático
val auth: AuthRepository = OmniFirebaseAmplify.createAuth() // Firebase Auth -> AWS Cognito
val database: FirestoreRepository = OmniFirebaseAmplify.createDatabase() // Firestore -> AWS DynamoDB

// 2. Acesso Direto aos Provedores
val firebaseStorage = OmniFirebaseAmplify.firebase.storage
val awsStorage = OmniFirebaseAmplify.amplify.storage // Amazon S3
```
