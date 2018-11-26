package org.kotlinbot.connectors.tg

import org.kotlinbot.api.inevents.*

data class TgMessageId(val id: Long) : MessageId {
    override val origin: Origin
        get() = Origin.TG
}

data class TgCallbackMessageId(val id: String) : MessageId {
    override val origin: Origin
        get() = Origin.TG
}

data class TgChatId(val id: Long) : ChatId {
    override val origin: Origin
        get() = Origin.TG
}

data class TgUserId(val id: Long, val isBot: Boolean) : UserId {
    override val kind: UserKind
        get() = if (isBot) UserKind.BOT else UserKind.HUMAN
    override val origin: Origin
        get() = Origin.TG
}