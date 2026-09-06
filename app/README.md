# 📱 Módulo :app — Consumidor & Demonstrador

O módulo **`:app`** é a aplicação Android de demonstração que exemplifica o consumo desacoplado dos contratos do **`:core`** e a injeção/comutação dinâmica entre os 8 drivers de backend suportados no **OmniBackend Android**.

---

## 🚀 Provedores Suportados na Demonstração

1. `:backend-firebase` (Google Firebase)
2. `:backend-supabase` (Supabase)
3. `:backend-appwrite` (Appwrite)
4. `:backend-back4app` (Back4App / Parse)
5. `:backend-pocketbase` (PocketBase)
6. `:backend-cloudflare` (Cloudflare Workers/D1/R2)
7. `:backend-amplify` (AWS Amplify/Cognito)
8. `:backend-rest` (Custom REST API)

---

## 🧪 Executar no Dispositivo

```bash
./gradlew :app:assembleDebug
```
