package org.kotlinbot.connectors.vk.longpoll

import kotlinx.coroutines.runBlocking
import name.alatushkin.api.vk.MethodExecutorImpl
import name.alatushkin.api.vk.api.fullId
import name.alatushkin.api.vk.callback.CallbackEvent
import name.alatushkin.api.vk.callback.MessageNew
import name.alatushkin.api.vk.generated.docs.Doc
import name.alatushkin.api.vk.generated.groups.LongPollServer
import name.alatushkin.api.vk.generated.messages.AudioMessage
import name.alatushkin.api.vk.generated.messages.Message
import name.alatushkin.api.vk.generated.photos.Photo
import name.alatushkin.api.vk.longpoll.SimpleServerLongPollEventSource
import name.alatushkin.api.vk.throwExceptionsOnError
import name.alatushkin.api.vk.withToken
import name.alatushkin.httpclient.httpClient
import org.kotlinbot.api.InEventConsumer
import org.kotlinbot.api.inevents.*
import org.kotlinbot.connectors.vk.vkDocToAttachment
import org.kotlinbot.connectors.vk.vkPhotoToAttachment

class VkLongPoller(
    val token: String,
    val groupId: Long,
    val eventConsumer: InEventConsumer
) {
    val httpClient = httpClient()
    val api = MethodExecutorImpl(httpClient).withToken(token).throwExceptionsOnError()
    val poller = SimpleServerLongPollEventSource(
        vkToken = token, httpClient = httpClient, groupId = groupId, timeOut = 100
    )

    fun doJob() = runBlocking {
        var iterator: LongPollServer? = null
        do {
            val (next, events) = poller.getEvents(iterator)
            iterator = next
            events.flatMap { convertToInEvents(it) }
                .forEach { eventConsumer(it) }
        } while (true)
    }

    private fun convertToInEvents(callbackEvent: CallbackEvent<*>): List<InEvent> {
        return when (callbackEvent) {
            is MessageNew -> convertMessageNew(callbackEvent.attachment)
            else -> emptyList()
        }
    }

    private fun convertMessageNew(inMessage: Message): List<InEvent> {
        val messageId: MessageId = VkMessageId(inMessage.id)
        val chatId: ChatId = VkChatId(inMessage.peerId)
        val userId: UserId = VkUserId(inMessage.fromId)

        val result = ArrayList<InEvent>()
        if (!inMessage.payload.isNullOrBlank()) {
            result.add(
                Callback(
                    messageId = messageId,
                    stringData = inMessage.payload!!,
                    userId = userId,
                    native = inMessage
                )
            )
        } else if (inMessage.text.isNotBlank()) {
            result.add(
                Text(
                    chatId = chatId,
                    messageId = messageId,
                    message = inMessage.text,
                    userId = userId,
                    kind = TextKind.UNKNOWN,
                    native = inMessage

                )
            )
        }

        if (inMessage.geo != null) {
            val geo = inMessage.geo
            val place = if (geo?.place != null)
                Place(geo.place!!.title!!, geo.place!!.address ?: "")
            else
                null

            val location = LatLng(geo!!.coordinates!!.latitude, geo.coordinates!!.longitude)

            result.add(
                InLocation(
                    chatId = chatId,
                    messageId = messageId,
                    userId = userId,
                    location = LocationAttachment(
                        origin = Origin.VK,
                        location = location,
                        place = place
                    ),
                    native = inMessage
                )
            )
        }

        val photos = inMessage.attachments?.filterIsInstance(Photo::class.java) ?: emptyList()
        if (photos.isNotEmpty()) {
            val attachments = photos.map {
                if (it.sizes?.isNotEmpty() == true) {
                    vkPhotoToAttachment(it)
                } else {
                    PhotoReferenceItem(
                        id = it.fullId(),
                        origin = messageId.origin
                    )
                }
            }
            result.add(
                Photos(
                    chatId = chatId,
                    messageId = messageId,
                    userId = userId,
                    photos = attachments,
                    native = inMessage
                )
            )
        }

        val voices = inMessage.attachments?.filterIsInstance(AudioMessage::class.java) ?: emptyList()

        voices.map {
            VoiceAttachment(
                origin = Origin.VK,
                id = it.fullId(),
                url = it.linkOgg,
                duration = it.duration.toInt()
            )
        }.map {
            Voice(
                chatId = chatId,
                messageId = messageId,
                userId = userId,
                voice = it,
                native = inMessage
            )
        }.forEach {
            result.add(it)
        }

        val files = inMessage.attachments?.filterIsInstance(Doc::class.java) ?: emptyList()

        files.map {
            vkDocToAttachment(it)

        }.forEach { attachment ->
            result.add(
                File(
                    chatId = chatId,
                    messageId = messageId,
                    userId = userId,
                    file = attachment,
                    native = inMessage
                )
            )
        }

        return result

    }
}

