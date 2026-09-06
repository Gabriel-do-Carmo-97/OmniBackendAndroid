package br.wgc.omnibackend.core.security

import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gerenciador corporativo de criptografia AES-256-GCM com chave simétrica mantida no Android Keystore.
 *
 * Utilizado para criptografar tokens de sessão, credenciais e payloads em repouso no dispositivo.
 */
class KeystoreCryptoManager(
    private val keyAlias: String = "OmniBackendMasterKey"
) {

    private val androidKeystore = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"
    private val gcmTagLength = 128

    init {
        generateMasterKeyIfNeeded()
    }

    private fun generateMasterKeyIfNeeded() {
        val keyStore = KeyStore.getInstance(androidKeystore).apply { load(null) }
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance("AES", androidKeystore)
            val spec = android.security.keystore.KeyGenParameterSpec.Builder(
                keyAlias,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(androidKeystore).apply { load(null) }
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    /**
     * Criptografa o texto em claro informado e retorna a combinação `IV:Ciphertext` em Base64.
     *
     * @param plainText Texto a ser protegido.
     * @return String codificada contendo vetor de inicialização e dados cifrados.
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        return "$ivBase64:$encryptedBase64"
    }

    /**
     * Descriptografa uma string codificada previamente produzida por [encrypt].
     *
     * @param encryptedData Payload no formato `IV:Ciphertext` em Base64.
     * @return Texto original descriptografado.
     */
    fun decrypt(encryptedData: String): String {
        val parts = encryptedData.split(":")
        require(parts.size == 2) { "Formato de payload criptografado inválido. Esperado 'IV:Ciphertext'" }
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(gcmTagLength, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
