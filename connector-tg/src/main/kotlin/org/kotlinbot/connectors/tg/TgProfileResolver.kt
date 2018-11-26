package org.kotlinbot.connectors.tg

import com.pengrad.telegrambot.model.Message
import org.kotlinbot.api.*
import org.kotlinbot.api.inevents.InEvent
import org.kotlinbot.api.inevents.InMessage

class TgProfileResolver : ProfileResolver {
    override suspend fun resolveProfileFromEvent(event: InEvent): UserProfile? {
        if (!event.origin.isTelegram)
            return null

        if (event !is InMessage)
            return null

        val from = (event.native as Message).from()

        return UserProfile(
            userId = TgUserId(from.id().toLong(), from.isBot),
            canSendMessage = CanSendMessage.YES,
            languageValue = ProfileValue(from.languageCode(), Source.PROFILE),
            firstNameValue = ProfileValue(from.firstName(), Source.PROFILE),
            lastNameValue = ProfileValue(from.lastName(), Source.PROFILE),
            resolved = true
        )

    }

}