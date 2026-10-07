# WGC OmniBackend Android — Live Templates para Android Studio

Este diretório contém os Live Templates e Code Snippets oficiais para acelerar o desenvolvimento de aplicações consumidoras do **OmniBackendAndroid**.

## 🚀 Snippets Disponíveis

| Abreviação | Descrição | Exemplo de Saída |
| :--- | :--- | :--- |
| `omniRepo` | Cria esqueleto de repositório padronizado com `DataResult` e injeção do Core | `class ExampleRepository @Inject constructor(...)` |
| `omniCircuit` | Cria bloco de chamada protegida com `CircuitBreaker` e fallback | `circuitBreaker.execute(action = { ... }, fallback = { ... })` |
| `omniRetry` | Cria bloco com política de retry exponencial (`ExponentialBackoffRetryPolicy`) | `retryPolicy.execute { ... }` |
| `omniAuth` | Bloco de autenticação de usuário com tratamento exaustivo de `DataResult` | `when (authResult) { is DataResult.Success -> ... }` |
| `omniTest` | Cria teste unitário com Coroutines (`runTest`) e `FakeAuthRepository` | `@Test fun should_return_user...` |

---

## 🛠️ Como Instalar no Android Studio

### Opção 1: Cópia Direta de Arquivo
Copie o arquivo `AndroidStudio_Omni_LiveTemplates.xml` para a pasta de templates do seu Android Studio:
- **Windows:** `%APPDATA%\Google\AndroidStudio<versao>\templates\`
- **macOS:** `~/Library/Application Support/Google/AndroidStudio<versao>/templates/`
- **Linux:** `~/.config/Google/AndroidStudio<versao>/templates/`

Em seguida, reinicie o Android Studio.

### Opção 2: Importação Manual
1. Abra o Android Studio.
2. Vá em **File** -> **Settings** (ou **Preferences** no macOS) -> **Editor** -> **Live Templates**.
3. Clique no ícone de engrenagem ou `+` e selecione **Import Settings...**
4. Aponte para `templates/AndroidStudio_Omni_LiveTemplates.xml` e clique em **Apply**.
