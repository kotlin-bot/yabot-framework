package org.kotlinbot.api.outevents

import org.kotlinbot.api.inevents.ChatId
import org.kotlinbot.api.inevents.Origin

interface OutEvent {
    val chatId: ChatId
    val origin: Origin get() = chatId.origin
}


