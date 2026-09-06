package br.wgc.omnibackend.firebase.data.model.database.message

import androidx.annotation.Keep

/**
 * Represents a single message within a conversation.
 */
@Keep
data class MessageRequest(
    val messageId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENDING
)

