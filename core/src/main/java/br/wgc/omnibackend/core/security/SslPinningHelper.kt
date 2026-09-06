package br.wgc.omnibackend.core.security

import java.security.MessageDigest
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Utilitário corporativo para validação de SSL/TLS Certificate Pinning por fingerprint SHA-256 da chave pública.
 */
object SslPinningHelper {

    /**
     * Converte os bytes da chave pública de um certificado em fingerprint SHA-256 no formato `sha256/Base64`.
     *
     * @param certificate Certificado X.509 recebido na conexão TLS.
     * @return Fingerprint no formato `sha256/Base64`.
     */
    fun computeSha256Pin(certificate: X509Certificate): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val publicKeyBytes = certificate.publicKey.encoded
        val hash = digest.digest(publicKeyBytes)
        val base64Hash = android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
        return "sha256/$base64Hash"
    }

    /**
     * Cria um [X509TrustManager] customizado que valida se pelo menos um dos pins informados corresponde
     * ao certificado do servidor TLS.
     *
     * @param expectedPins Lista de hashes permitidos no formato `sha256/Base64`.
     * @param defaultTrustManager TrustManager padrão do sistema.
     * @return Instance de [X509TrustManager] com verificação de pinning.
     */
    fun createPinningTrustManager(
        expectedPins: Set<String>,
        defaultTrustManager: X509TrustManager
    ): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                defaultTrustManager.checkClientTrusted(chain, authType)
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                defaultTrustManager.checkServerTrusted(chain, authType)
                if (chain.isNullOrEmpty()) {
                    throw javax.net.ssl.SSLHandshakeException("Cadeia de certificados do servidor está vazia.")
                }
                val match = chain.any { cert ->
                    val pin = computeSha256Pin(cert)
                    expectedPins.contains(pin)
                }
                if (!match && expectedPins.isNotEmpty()) {
                    throw javax.net.ssl.SSLHandshakeException("Falha no SSL Pinning: certificado do servidor não corresponde aos pins esperados.")
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return defaultTrustManager.acceptedIssuers
            }
        }
    }
}
