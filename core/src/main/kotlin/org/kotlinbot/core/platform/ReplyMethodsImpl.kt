package org.kotlinbot.core.platform


import org.kotlinbot.api.inevents.ChatId
import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.methods.ReplyMethods
import org.kotlinbot.api.outevents.*

class ReplyMethodsImpl(
    val chatId: ChatId,
    val messageId: MessageId? = null
) : ReplyMethods {
    val eventsQueue = ArrayList<OutMessage>()
    override fun send(message: OutMessage) {
        eventsQueue.add(message)
    }

    override fun send(messageText: String, keyboard: Keyboard?) =
        send(TextMessage(chatId = chatId, keyboard = keyboard, text = messageText))

    override fun reply(messageText: String, keyboard: Keyboard?) {
        assert(messageId != null) { "Cant reply to non text message" }
        send(messageText, keyboard)
    }

    override fun send(vararg photo: PhotoAttachment, keyboard: Keyboard?) {
        send(Photos(chatId = chatId, keyboard = keyboard, photos = photo.toList()))
    }

    override fun reply(vararg photo: PhotoAttachment, keyboard: Keyboard?) {
        assert(messageId != null) { "Cant reply to non text message" }
        send(
            Photos(
                chatId = chatId,
                keyboard = keyboard,
                photos = photo.toList(),
                replyTo = messageId
            )
        )
    }

    override fun send(vararg files: FileAttachment, keyboard: Keyboard?) {
        send(Files(chatId = chatId, keyboard = keyboard, files = files.toList()))
    }

    override fun reply(vararg files: FileAttachment, keyboard: Keyboard?) {
        assert(messageId != null) { "Cant reply to non text message" }
        send(
            Files(
                chatId = chatId,
                keyboard = keyboard,
                files = files.toList(),
                replyTo = messageId
            )
        )
    }

    override fun editMessage(messageId: MessageId, messageText: String, keyboard: Keyboard?) {
        send(TextMessage(chatId, null, keyboard, messageId, messageText))
    }

}