package org.kotlinbot.api

import org.kotlinbot.api.inevents.ChatId
import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.inevents.UserId

interface Scope : org.kotlinbot.api.inevents.Source {
    val botId: BotId
    val chatId: ChatId
    val userId: UserId
    val profile: UserProfile
    val messageId: MessageId?

    override val isTelegram: Boolean
        get() = chatId.origin.isTelegram
    override val isVkontakte: Boolean
        get() = chatId.origin.isVkontakte
    override val isViber: Boolean
        get() = chatId.origin.isViber
    override val isFacebook: Boolean
        get() = chatId.origin.isFacebook
    override val isInstagram: Boolean
        get() = chatId.origin.isInstagram
}