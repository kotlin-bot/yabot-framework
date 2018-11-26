package org.kotlinbot.api

import org.kotlinbot.api.inevents.ChatId
import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.inevents.UserId

interface Scope {
    //val botId: BotId
    val chatId: ChatId
    val userId: UserId
    val profile: UserProfile
    val messageId: MessageId?
}