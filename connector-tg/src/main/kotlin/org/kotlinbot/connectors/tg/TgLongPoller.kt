package org.kotlinbot.connectors.tg

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.*
import com.pengrad.telegrambot.request.GetUpdates
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.kotlinbot.api.InEventConsumer
import org.kotlinbot.api.inevents.*
import org.kotlinbot.api.inevents.Voice
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


class TgLongPoller(
    token: String,
    val eventConsumer: InEventConsumer,
    val timeOut: Int = 5
) {
    val okHttpClient = OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).build()
    val bot = TelegramBot.Builder(token).okHttpClient(okHttpClient).build()

    fun doJob() {
        var nextOffset: Int = 0

        do {
            try {
                val (offset, updates) = getUpdates(nextOffset)
                updates
                    .flatMap { convertToInEvents(it) }
                    .forEach { runBlocking { eventConsumer(it) } }
                nextOffset = offset
            } catch (e: ConnectException) {
                logger.warn("Connection problem. Cant connect to tg api. Maybe you need proxy?")
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: SocketTimeoutException) {
                logger.warn("Yet another socket timeout in Tg Poll")
            } catch (e: SocketException) {
                logger.warn("Yet another socket timeout in Tg Poll")
            } catch (e: Exception) {
                if(e.cause is SocketTimeoutException)
                    logger.warn("Yet another socket timeout in Tg Poll")
                else
                    logger.warn("Exception on longpoll cycle {}", e)
            }
        } while (!Thread.interrupted())
    }


    private fun getUpdates(offset: Int = 0): Pair<Int, List<Update>> {
        val getUpdates = GetUpdates().limit(100).offset(offset).timeout(timeOut)
        val updates = bot.execute(getUpdates)

        val nextUpdateId = updates.updates().lastOrNull()?.updateId()?.inc() ?: 0
        return nextUpdateId to updates.updates()
    }

    private fun convertToInEvents(update: Update): List<InEvent> {
        return when {
            update.message() != null -> convertNewMessage(update.message())
            update.callbackQuery() != null -> convertCallbackQuery(update.callbackQuery())
            update.inlineQuery() != null -> convertInlineQuery(update.inlineQuery())
            update.chosenInlineResult() != null -> convertChoiseInlineResult(update.chosenInlineResult())
            else -> emptyList()
        }
    }

    private fun convertChoiseInlineResult(inlineResult: ChosenInlineResult): List<InEvent> {
        return emptyList()
    }

    private fun convertInlineQuery(inlineQuery: InlineQuery): List<InEvent> {
        return emptyList()
    }

    private fun convertCallbackQuery(callbackQuery: CallbackQuery): List<InEvent> {
        val messageId = TgCallbackMessageId(callbackQuery.id())
        val userId = TgUserId(callbackQuery.from().id().toLong(), callbackQuery.from().isBot)

        return listOf(
            Callback(
                messageId = messageId,
                personId = userId,
                native = callbackQuery,
                stringData = callbackQuery.data()
            )
        )
    }

    private fun convertNewMessage(message: Message): List<InEvent> {
        val messageId = TgMessageId(message.messageId().toLong())
        val userId = TgUserId(message.from().id().toLong(), message.from().isBot)
        val chatId = TgChatId(message.chat().id())
        return when {
            message.text() != null -> {
                listOf(
                    Text(
                        message = message.text(),
                        kind = TextKind.UNKNOWN,
                        messageId = messageId,
                        personId = userId,
                        chatId = chatId,
                        native = message
                    )
                )
            }

            message.photo() != null -> listOf(
                Photos(
                    personId = userId,
                    chatId = chatId,
                    native = message,
                    messageId = messageId,
                    photos = listOf(resolvePhoto(message.photo().sortedByDescending { it.width() }.first()))
                )
            )

            message.voice() != null -> listOf(
                Voice(
                    personId = userId,
                    chatId = chatId,
                    native = message,
                    messageId = messageId,
                    voice = VoiceReferenceItem(id = message.voice().fileId(), origin = Origin.TG)
                )
            )

            message.location() != null -> listOf(
                InLocation(
                    personId = userId,
                    chatId = chatId,
                    native = message,
                    messageId = messageId,
                    location = LocationAttachment(
                        origin = Origin.TG,
                        location = LatLng(
                            message.location().latitude().toDouble(),
                            message.location().longitude().toDouble()
                        )
                    )
                )
            )

            message.venue() != null -> listOf(
                InLocation(
                    personId = userId,
                    chatId = chatId,
                    native = message,
                    messageId = messageId,
                    location = LocationAttachment(
                        origin = Origin.TG,
                        location = LatLng(
                            message.venue().location().latitude().toDouble(),
                            message.venue().location().longitude().toDouble()
                        ),
                        place = Place(message.venue().title(), message.venue().address())
                    )
                )
            )


            else -> emptyList()
        }

    }

    private fun resolvePhoto(photo: PhotoSize): PhotoReference {
        return PhotoReferenceItem(
            id = photo.fileId(),
            origin = Origin.TG,
            width = photo.width(),
            height = photo.height()
        )
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(TgLongPoller::class.java)
    }
}