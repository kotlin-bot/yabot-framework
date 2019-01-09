package org.kotlinbot.api.inevents

enum class TextKind {
    UNKNOWN, QUESTION
}

data class Text(
    override val messageId: MessageId,
    override val personId: PersonId,
    override val chatId: ChatId,
    val message: String,
    val kind: TextKind,
    override val native: Any
) : InMessage