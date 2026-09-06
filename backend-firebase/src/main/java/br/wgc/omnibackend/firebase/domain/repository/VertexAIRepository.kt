package br.wgc.omnibackend.firebase.domain.repository

import br.wgc.omnibackend.firebase.utils.DataResult
import kotlinx.coroutines.flow.Flow

interface VertexAIRepository {
    suspend fun generateText(
        prompt: String,
        modelName: String = "gemini-1.5-flash"
    ): DataResult<String>

    fun generateTextStream(
        prompt: String,
        modelName: String = "gemini-1.5-flash"
    ): Flow<DataResult<String>>
}
