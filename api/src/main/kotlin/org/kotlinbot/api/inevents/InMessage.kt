package org.kotlinbot.api.inevents

interface InMessage : InEvent {
    val messageId: MessageId
    val native: Any

    override val origin: Origin
        get() = chatId.origin

}

