package org.kotlinbot.api.inevents

import com.fasterxml.jackson.annotation.JsonTypeInfo

enum class UserKind {
    HUMAN, PUBLIC
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
interface PersonId : ChatId {
    val kind: UserKind

    companion object
}