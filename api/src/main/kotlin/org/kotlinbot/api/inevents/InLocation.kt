package org.kotlinbot.api.inevents

data class InLocation(
    override val messageId: MessageId,
    override val personId: PersonId,
    override val chatId: ChatId,
    val location: LocationReference,
    override val native: Any
) : InMessage