package org.kotlinbot.api.methods

import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.outevents.FileAttachment
import org.kotlinbot.api.outevents.Keyboard
import org.kotlinbot.api.outevents.OutMessage
import org.kotlinbot.api.outevents.PhotoAttachment

interface ReplyMethods {

    fun send(message: OutMessage)
    fun send(messageText: String, keyboard: Keyboard? = null)
    fun reply(messageText: String, keyboard: Keyboard? = null)
    fun send(vararg photo: PhotoAttachment, keyboard: Keyboard? = null)
    fun reply(vararg photo: PhotoAttachment, keyboard: Keyboard? = null)

    fun send(vararg files: FileAttachment, keyboard: Keyboard? = null)
    fun reply(vararg files: FileAttachment, keyboard: Keyboard? = null)

    fun editMessage(messageId: MessageId, messageText: String, keyboard: Keyboard? = null)
}

