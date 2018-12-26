package org.kotlinbot.api.inevents

import com.fasterxml.jackson.annotation.JsonTypeInfo

enum class UserKind {
    HUMAN, PUBLIC, BOT
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
interface UserId : ChatId {
    val kind: UserKind

    companion object
}