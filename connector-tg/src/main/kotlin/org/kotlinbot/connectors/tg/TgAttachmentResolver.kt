package org.kotlinbot.connectors.tg

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.GetFile
import org.kotlinbot.api.inevents.*
import org.kotlinbot.api.methods.AttachmentResolver

class TgAttachmentResolver(val tgToken: String) : AttachmentResolver {
    val bot = TelegramBot(tgToken)

    override suspend fun resolve(attacheReference: PhotoReference): PhotoAttachment {
        return PhotoAttachment(
            origin = Origin.TG,
            id = attacheReference.id,
            fileUrl = fullUrl(bot.execute(GetFile(attacheReference.id)).file().filePath()),
            height = attacheReference.width ?: 0,
            width = attacheReference.height ?: 0
        )

    }

    override suspend fun resolve(attacheReference: LocationReference): LocationAttachment {
        return attacheReference as LocationAttachment
    }

    override suspend fun resolve(attacheReference: FileReference): FileAttachment {
        val file = bot.execute(GetFile(attacheReference.id)).file()
        return FileAttachment(
            origin = Origin.TG,
            id = attacheReference.id,
            url = fullUrl(file.filePath()),
            size = file.fileSize(),
            title = "",
            extension = ""
        )
    }

    override suspend fun resolve(attacheReference: VoiceReference): VoiceAttachment {
        val file = bot.execute(GetFile(attacheReference.id)).file()
        return VoiceAttachment(
            origin = Origin.TG,
            id = attacheReference.id,
            url = fullUrl(file.filePath()),
            duration = 1
        )
    }

    fun fullUrl(fileUrl:String):String{
        return "https://api.telegram.org/file/bot$tgToken/$fileUrl"
    }
}