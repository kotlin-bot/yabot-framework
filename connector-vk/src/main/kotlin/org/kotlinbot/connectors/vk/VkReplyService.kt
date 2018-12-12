package org.kotlinbot.connectors.vk

import kotlinx.coroutines.delay
import name.alatushkin.api.vk.MethodExecutorWithException
import name.alatushkin.api.vk.api.sendTypings
import name.alatushkin.api.vk.api.toAttachmentId
import name.alatushkin.api.vk.api.utils.upload.uploadMessageDocument
import name.alatushkin.api.vk.api.utils.upload.uploadMessagePhoto
import name.alatushkin.api.vk.generated.messages.*
import name.alatushkin.api.vk.generated.messages.methods.MessagesSendMethod
import name.alatushkin.api.vk.generated.messages.methods.MessagesSetActivityMethod
import org.kotlinbot.api.ReplyService
import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.outevents.*
import org.kotlinbot.api.outevents.Keyboard
import org.kotlinbot.connectors.vk.longpoll.VkChatId
import org.kotlinbot.connectors.vk.longpoll.VkId
import org.kotlinbot.connectors.vk.longpoll.VkMessageId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Long.max
import kotlin.random.Random

class VkReplyService(
    val groupId: String,
    val vkMethodExutor: MethodExecutorWithException
) : ReplyService {

    override suspend fun invoke(outEvent: OutEvent): MessageId? {

        return when (outEvent) {
            is TextMessage -> convertAndSendText(groupId, vkMethodExutor, outEvent)
            is Photos -> convertAndSendPhots(groupId, vkMethodExutor, outEvent)
            is Files -> convertAndSendFiles(groupId, vkMethodExutor, outEvent)
            is CallbackReply -> null
            else -> error("Unknonwn to convet type")
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(VkReplyService::class.java)
    }
}


suspend fun convertAndSendPhots(senderId: String, api: MethodExecutorWithException, photos: Photos): MessageId {
    val peerId = (photos.chatId as VkChatId).id
    val vkApi = api.asMethodExecutor()

    vkApi.sendTypings(senderId.toLong(), peerId)
    val attchments = photos.photos.map { photo ->
        vkApi.uploadMessagePhoto(peerId, (photo as Attachment).toBytesAttachment(api.httpClient).bytes).toAttachmentId()
    }.toTypedArray()
    return VkMessageId(
        api(
            MessagesSendMethod(
                randomId = Random.nextLong(),
                //chatId = (textMessage.chatId as VkChatId).id,
                peerId = peerId,
                groupId = senderId.toLong(),
                attachment = attchments,
                keyboard = toVkKeyboard(photos.keyboard)
            )
        )
    )

}

suspend fun convertAndSendFiles(senderId: String, api: MethodExecutorWithException, files: Files): MessageId {
    val peerId = (files.chatId as VkChatId).id
    val vkApi = api.asMethodExecutor()

    vkApi.sendTypings(senderId.toLong(), peerId)
    val attchments = files.files.map { file ->
        vkApi.uploadMessageDocument(
            peerId,
            (file as Attachment).fileName,
            file.toBytesAttachment(api.httpClient).bytes
        ).toAttachmentId()
    }.toTypedArray()
    return VkMessageId(
        api(
            MessagesSendMethod(
                randomId = Random.nextLong(),
                //chatId = (textMessage.chatId as VkChatId).id,
                peerId = peerId,
                groupId = senderId.toLong(),
                attachment = attchments,
                keyboard = toVkKeyboard(files.keyboard)
            )
        )
    )

}


suspend fun convertAndSendText(
    senderId: String,
    api: MethodExecutorWithException,
    textMessage: TextMessage
): MessageId {

    val chatId = textMessage.chatId as VkId
    api.asMethodExecutor().sendTypings(senderId.toLong(), chatId.id)

    val pause = max(300, textMessage.text.length * 1000 / 100 / 60.toLong())
    delay(pause)

    api(
        MessagesSetActivityMethod(
            peerId = chatId.id,
            type = "",
            groupId = senderId.toLong()
        )
    )

    val messageId = api.invoke(
        MessagesSendMethod(
            randomId = Random.nextLong(),
            peerId = chatId.id,
            message = textMessage.text,
            groupId = senderId.toLong(),
            keyboard = toVkKeyboard(textMessage.keyboard)
        )
    )

    return VkMessageId(messageId)
}

fun toVkKeyboard(keyboard: Keyboard?): name.alatushkin.api.vk.generated.messages.Keyboard? {
    if (keyboard == null)
        return null

    return KeyboardImpl(
        oneTime = keyboard.oneTime,
        buttons = keyboard.buttons.mapIndexedNotNull { rowIdx, row ->
            if (rowIdx < 10) {
                row.mapIndexedNotNull { colIdx, btn ->
                    if (colIdx < 4) {
                        val buttonAction = KeyboardButtonAction(
                            type = KeyboardButtonActionType.TEXT,
                            label = btn.label,
                            payload = if (btn is CallbackButton) "\"${btn.data}\"" else null
                        )

                        KeyboardButton(
                            color = KeyboardButtonColor.DEFAULT,
                            action = buttonAction
                        )
                    } else {
                        VkReplyService.logger.warn("Key idx {} {} was ignored in VK", colIdx, btn.label)
                        null
                    }
                }.toTypedArray()
            } else {
                VkReplyService.logger.warn("Row ow keyboard idx {}  was ignored in VK {}", rowIdx, row)
                null
            }

        }.toTypedArray()
    )
}