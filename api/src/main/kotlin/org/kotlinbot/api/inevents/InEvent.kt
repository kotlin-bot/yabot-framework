package org.kotlinbot.api.inevents


interface InEvent{
    val personId: PersonId
    val chatId: ChatId
    val origin:Origin
}