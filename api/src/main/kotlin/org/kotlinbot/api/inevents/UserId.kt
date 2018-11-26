package org.kotlinbot.api.inevents

enum class UserKind {
    HUMAN, PUBLIC, BOT
}

interface UserId : ChatId {
    val kind: UserKind

    companion object
}