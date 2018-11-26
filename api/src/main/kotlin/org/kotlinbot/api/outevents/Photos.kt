package org.kotlinbot.api.outevents

import org.kotlinbot.api.inevents.ChatId
import org.kotlinbot.api.inevents.MessageId

data class Photos(
    override val chatId: ChatId,
    override val replyTo: MessageId? = null,
    override val keyboard: Keyboard?,
    override val updateMessage: MessageId? = null,
    val photos: List<PhotoAttachment>
) : OutMessage