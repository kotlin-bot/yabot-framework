package org.kotlinbot.api.outevents

import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.inevents.PersonId

data class Sticker(
    override val chatId: PersonId,
    override val replyTo: MessageId? = null,
    override val keyboard: Keyboard?,
    override val updateMessage: MessageId? = null,
    val sticker: String
) : OutMessage