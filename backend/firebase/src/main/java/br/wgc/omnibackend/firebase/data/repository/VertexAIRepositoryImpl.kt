package br.wgc.omnibackend.firebase.data.repository

import br.wgc.omnibackend.core.repository.VertexAIRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.google.firebase.vertexai.FirebaseVertexAI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

/**
 * Implementação do contrato [VertexAIRepository] utilizando a biblioteca Firebase Vertex AI (Gemini).
 *
 * @property vertexAI Instância do [FirebaseVertexAI] injetada.
 */
class VertexAIRepositoryImpl @Inject constructor(
    private val vertexAI: FirebaseVertexAI
) : VertexAIRepository {

    /**
     * Gera uma resposta em texto completo a partir do prompt e modelo especificados.
     *
     * @param prompt Texto descritivo com a instrução para a IA.
     * @param modelName Identificador do modelo generativo (padrão "gemini-1.5-flash").
     * @return [DataResult.Success] com o texto retornado pela IA.
     */
    override suspend fun generateText(
        prompt: String,
        modelName: String
    ): DataResult<String> = runCatching {
        val model = vertexAI.generativeModel(modelName)
        val response = model.generateContent(prompt)
        val text = response.text ?: throw IllegalStateException("Resposta vazia da IA.")
        DataResult.Success(text)
    }.getOrElse { exception ->
        DataResult.Failure(mapExceptionToAppError(exception))
    }

    /**
     * Gera um fluxo de streaming de texto emitindo partes da resposta incrementalmente.
     *
     * @param prompt Instrução para a IA.
     * @param modelName Nome do modelo generativo.
     * @return [Flow] que emite blocos textuais à medida que são concluídos.
     */
    override fun generateTextStream(
        prompt: String,
        modelName: String
    ): Flow<DataResult<String>> = flow {
        val model = vertexAI.generativeModel(modelName)
        runCatching {
            model.generateContentStream(prompt).collect { chunk ->
                chunk.text?.let { textChunk ->
                    emit(DataResult.Success(textChunk))
                }
            }
        }.onFailure { exception ->
            emit(DataResult.Failure(mapExceptionToAppError(exception)))
        }
    }

    private fun mapExceptionToAppError(exception: Throwable): AppError {
        return when (exception) {
            is IOException -> AppError.Generic.Network
            is Exception -> {
                val message = exception.message.orEmpty()
                when {
                    message.contains("blocked", ignoreCase = true) -> AppError.VertexAI.ResponseBlocked
                    message.contains("quota", ignoreCase = true) -> AppError.VertexAI.QuotaExceeded
                    message.contains("key", ignoreCase = true) -> AppError.VertexAI.InvalidApiKey
                    else -> AppError.VertexAI.Generic(exception)
                }
            }
            else -> AppError.Generic.Unknown(exception)
        }
    }
}
