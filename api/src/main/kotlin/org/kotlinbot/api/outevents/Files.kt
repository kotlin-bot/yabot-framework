package org.kotlinbot.api.outevents

import org.kotlinbot.api.inevents.ChatId
import org.kotlinbot.api.inevents.MessageId

data class Files(
    override val chatId: ChatId,
    override val replyTo: MessageId? = null,
    override val keyboard: Keyboard?,
    override val updateMessage: MessageId? = null,
    val files: List<FileAttachment>
) : OutMessage