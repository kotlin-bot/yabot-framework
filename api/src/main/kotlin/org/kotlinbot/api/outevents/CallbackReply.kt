package org.kotlinbot.api.outevents

import org.kotlinbot.api.inevents.ChatId
import org.kotlinbot.api.inevents.MessageId

data class CallbackReply(
    override val chatId: ChatId,
    val replyId: MessageId,
    val message: String? = null
) : OutMessage {
    override val replyTo: MessageId?
        get() = null
    override val keyboard: Keyboard?
        get() = null
    override val updateMessage: MessageId?
        get() = null
}