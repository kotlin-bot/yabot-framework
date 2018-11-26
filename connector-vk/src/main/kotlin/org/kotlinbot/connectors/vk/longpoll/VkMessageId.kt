package org.kotlinbot.connectors.vk.longpoll

import org.kotlinbot.api.inevents.*

data class VkMessageId(val id: Long) : MessageId {
    override val origin: Origin
        get() = Origin.VK
}

interface VkId{
    val id:Long
}

data class VkChatId(override val id: Long) : ChatId,VkId {
    override val origin: Origin
        get() = Origin.VK
}

data class VkUserId(override val id: Long) : UserId,VkId {
    override val kind: UserKind
        get() = if (id > 0) UserKind.HUMAN else UserKind.PUBLIC

    override val origin: Origin
        get() = Origin.VK
}