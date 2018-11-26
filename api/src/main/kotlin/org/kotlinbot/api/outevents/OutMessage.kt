package org.kotlinbot.api.outevents

import org.kotlinbot.api.inevents.MessageId

interface OutMessage : OutEvent {
    val replyTo: MessageId?
    val keyboard: Keyboard?
    val updateMessage: MessageId?
    val keepSilance: Boolean get() = false
}