package org.kotlinbot.api.inevents

data class Photos(
    override val messageId: MessageId,
    override val personId: PersonId,
    override val chatId: ChatId,
    val photos: Collection<PhotoReference>,
    override val native: Any
) : InMessage