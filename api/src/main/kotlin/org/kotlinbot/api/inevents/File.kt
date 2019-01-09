package org.kotlinbot.api.inevents

data class File(
    override val messageId: MessageId,
    override val personId: PersonId,
    override val chatId: ChatId,
    val file: FileReference,
    override val native: Any
) : InMessage