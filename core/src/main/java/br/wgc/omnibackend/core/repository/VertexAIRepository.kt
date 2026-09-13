package br.wgc.omnibackend.core.repository

import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato agnóstico para modelos de inteligência artificial generativa (Gemini / Vertex AI).
 */
interface VertexAIRepository {

    /**
     * Gera uma resposta de texto a partir de um prompt fornecido.
     *
     * @param prompt Texto descritivo de instrução para a IA.
     * @param modelName Nome do modelo generativo (padrão "gemini-1.5-flash").
     * @return [DataResult.Success] contendo o texto gerado pela IA.
     */
    suspend fun generateText(prompt: String, modelName: String = "gemini-1.5-flash"): DataResult<String>

    /**
     * Gera uma resposta textual contínua em formato de streaming (token por token).
     *
     * @param prompt Texto descritivo de instrução.
     * @param modelName Nome do modelo generativo.
     * @return [Flow] que emite blocos textuais à medida que são gerados pelo modelo.
     */
    fun generateTextStream(prompt: String, modelName: String = "gemini-1.5-flash"): Flow<DataResult<String>>
}
