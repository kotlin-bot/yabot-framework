package org.kotlinbot.api.methods

import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.outevents.FileAttachment
import org.kotlinbot.api.outevents.Keyboard
import org.kotlinbot.api.outevents.OutMessage
import org.kotlinbot.api.outevents.PhotoAttachment

interface ReplyNowMethods {
    suspend fun sendNow(message: OutMessage): MessageId
    suspend fun sendNow(messageText: String, keyboard: Keyboard? = null): MessageId
    suspend fun replyNow(messageText: String, keyboard: Keyboard? = null): MessageId
    suspend fun sendNow(vararg photo: PhotoAttachment, keyboard: Keyboard? = null): MessageId
    suspend fun replyNow(vararg photo: PhotoAttachment, keyboard: Keyboard? = null): MessageId
    suspend fun sendNow(vararg files: FileAttachment, keyboard: Keyboard? = null): MessageId
    suspend fun replyNow(vararg files: FileAttachment, keyboard: Keyboard? = null): MessageId
}
