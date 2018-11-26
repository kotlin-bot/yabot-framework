package org.kotlinbot.api.inevents


interface InEvent{
    val userId: UserId
    val chatId: ChatId
    val origin:Origin
}