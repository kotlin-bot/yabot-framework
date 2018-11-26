package org.kotlinbot.core

import org.kotlinbot.api.BotId
import org.kotlinbot.api.BotState
import org.kotlinbot.api.inevents.UserId

interface BotStateRepository {
    suspend fun get(botId: BotId, userId: UserId): BotState
    suspend fun set(botId: BotId, userId: UserId, botState: BotState)
    suspend fun allStates(botId: BotId):List<BotState>
}
