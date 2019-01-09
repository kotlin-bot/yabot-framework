package org.kotlinbot.api.inevents

data class Sticker(
    override val messageId: MessageId,
    override val personId: PersonId,
    override val chatId: ChatId,
    override val native: Any
) : InMessage