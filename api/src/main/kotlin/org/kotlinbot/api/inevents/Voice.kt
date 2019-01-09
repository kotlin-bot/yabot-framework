package org.kotlinbot.api.inevents

data class Voice(
    override val messageId: MessageId,
    override val personId: PersonId,
    override val chatId: ChatId,
    val voice: VoiceReference,
    override val native: Any
) : InMessage