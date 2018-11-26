package org.kotlinbot.core.platform

import org.kotlinbot.api.ReplyService
import org.kotlinbot.api.inevents.ChatId
import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.methods.ReplyNowMethods
import org.kotlinbot.api.outevents.*

class ReplyNowMethodsImpl(
    val chatId: ChatId,
    val messageId: MessageId? = null,
    val replyService: ReplyService
) : ReplyNowMethods {


    override suspend fun sendNow(message: OutMessage): MessageId {
        return replyService(message)!!
    }

    override suspend fun sendNow(messageText: String, keyboard: Keyboard?): MessageId {
        return sendNow(
            TextMessage(
                chatId = chatId,
                keyboard = keyboard,
                text = messageText
            )
        )
    }

    override suspend fun replyNow(messageText: String, keyboard: Keyboard?): MessageId {
        assert(messageId != null) { "Cant reply to non text message" }
        return sendNow(messageText, keyboard)
    }

    override suspend fun sendNow(vararg photo: PhotoAttachment, keyboard: Keyboard?): MessageId {
        assert(messageId != null) { "Cant reply to non text message" }
        return sendNow(
            Photos(
                chatId = chatId,
                keyboard = keyboard,
                photos = photo.toList()
            )
        )
    }

    override suspend fun replyNow(vararg photo: PhotoAttachment, keyboard: Keyboard?): MessageId {
        assert(messageId != null) { "Cant reply to non text message" }
        return sendNow(
            Photos(
                chatId = chatId,
                keyboard = keyboard,
                photos = photo.toList(),
                replyTo = messageId
            )
        )
    }

    override suspend fun sendNow(vararg files: FileAttachment, keyboard: Keyboard?): MessageId {
        TODO("not implemented") //To change body intentHandlerOf created functions use File | Settings | File Templates.
    }

    override suspend fun replyNow(vararg files: FileAttachment, keyboard: Keyboard?): MessageId {
        TODO("not implemented") //To change body intentHandlerOf created functions use File | Settings | File Templates.
    }
}