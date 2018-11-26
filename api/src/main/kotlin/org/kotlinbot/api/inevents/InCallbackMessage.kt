package org.kotlinbot.api.inevents

interface InCallbackMessage : InEvent {
    val messageId: MessageId
    val native: Any
}