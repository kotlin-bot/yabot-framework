package org.kotlinbot.api.inevents


data class Callback(
    override val messageId: MessageId,
    override val userId: UserId,
    val stringData: String,
    override val native: Any
) : InCallbackMessage {
    override val chatId: ChatId
        get() = userId

    override val origin: Origin
        get() = userId.origin
}