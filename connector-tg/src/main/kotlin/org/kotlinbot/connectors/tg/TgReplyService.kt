package org.kotlinbot.connectors.tg

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.*
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.request.*
import kotlinx.coroutines.runBlocking
import name.alatushkin.httpclient.HttpClient
import okhttp3.OkHttpClient
import org.kotlinbot.api.ReplyService
import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.outevents.*
import java.util.concurrent.TimeUnit


class TgReplyService(
    val token: String,
    val httpClient: HttpClient
) : ReplyService {
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .writeTimeout(1, TimeUnit.SECONDS)
        .build()
    val bot = TelegramBot.Builder(token).okHttpClient(okHttpClient).build()

    override suspend fun invoke(outEvent: OutEvent): MessageId? {
        return when (outEvent) {
            is TextMessage -> convertAndSendText(outEvent)
            is Photos -> convertAndSendPhots(outEvent)
            is Files -> convertAndSendFiles(outEvent)
            is CallbackReply -> convertAndSendCallbackReply(outEvent)
            else -> error("Unknonwn to convet type")
        }
    }

    private fun convertAndSendCallbackReply(outEvent: CallbackReply): MessageId? {
        val query = AnswerCallbackQuery((outEvent.replyId as TgCallbackMessageId).id)
        if (outEvent.message != null)
            query.text(outEvent.message)
        bot.execute(query)
        return null
    }

    private fun convertAndSendFiles(outEvent: Files): MessageId {
        val chatId = (outEvent.chatId as TgChatId).id
        return outEvent.files.map { file ->
            runBlocking {
                bot.execute(SendChatAction(chatId, ChatAction.upload_photo))
                val request = SendDocument(chatId, (file as Attachment).toBytesAttachment(httpClient).bytes)

                if (outEvent.replyTo != null) {
                    request.replyToMessageId((outEvent.replyTo as TgMessageId).id.toInt())
                }

                if (outEvent.keyboard != null) {
                    request.replyMarkup(convertKeyboard(outEvent.keyboard!!))
                }

                bot.execute(request).message()
                    .messageId()
            }
        }.last().let { TgMessageId(it.toLong()) }
    }

    private fun convertAndSendPhots(outEvent: Photos): MessageId {
        val chatId = (outEvent.chatId as TgChatId).id
        return outEvent.photos.map { photo ->
            runBlocking {
                bot.execute(SendChatAction(chatId, ChatAction.upload_photo))
                val request = SendPhoto(chatId, (photo as Attachment).toBytesAttachment(httpClient).bytes)

                if (outEvent.replyTo != null) {
                    request.replyToMessageId((outEvent.replyTo as TgMessageId).id.toInt())
                }

                if (outEvent.keyboard != null) {
                    request.replyMarkup(convertKeyboard(outEvent.keyboard!!))
                }

                bot.execute(request).message()
                    .messageId()
            }
        }.last().let { TgMessageId(it.toLong()) }
    }

    private fun convertAndSendText(outEvent: TextMessage): MessageId {
        val request = SendMessage((outEvent.chatId as TgChatId).id, outEvent.text)
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(true)
            .disableNotification(outEvent.keepSilance)

        if (outEvent.replyTo != null) {
            request.replyToMessageId((outEvent.replyTo as TgMessageId).id.toInt())
        }

        if (outEvent.keyboard != null) {
            request.replyMarkup(convertKeyboard(outEvent.keyboard!!))
        }

        return TgMessageId(bot.execute(request).message().messageId().toLong())
    }

    private fun convertKeyboard(keyboard: org.kotlinbot.api.outevents.Keyboard): Keyboard {
        return if (keyboard.inline)
            InlineKeyboardMarkup(*keyboard.buttons.map { row ->
                row.filter { it !is LocationButton }
                    .mapNotNull { button ->
                        val result = InlineKeyboardButton(button.label)
                        when (button) {
                            is TextButton -> result.callbackData(button.label)
                            is CallbackButton -> result.callbackData(button.data)
                            is OpenUrlButton -> result.url(button.url)
                            else -> null
                        }
                    }.toTypedArray()
            }.toTypedArray())
        else
            ReplyKeyboardMarkup(*keyboard.buttons.map { row ->
                row.filter { it !is LocationButton && it !is OpenUrlButton }
                    .mapNotNull { button ->
                        val result = KeyboardButton(button.label)
                        when (button) {
                            is TextButton -> result
                            is CallbackButton -> null
                            is OpenUrlButton -> null
                            is LocationButton -> result.requestLocation(true)
                        }
                    }.toTypedArray()
            }.toTypedArray()).oneTimeKeyboard(keyboard.oneTime).resizeKeyboard(true)
    }
}