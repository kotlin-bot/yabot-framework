package org.kotlinbot.api.inevents


data class Callback(
    override val messageId: MessageId,
    override val personId: PersonId,
    val stringData: String,
    override val native: Any
) : InCallbackMessage {
    override val chatId: ChatId
        get() = personId

    override val origin: Origin
        get() = personId.origin
}