package org.kotlinbot.core

import org.kotlinbot.api.BotId
import org.kotlinbot.api.BotState
import org.kotlinbot.api.inevents.PersonId

interface BotStateRepository {
    suspend fun get(botId: BotId, userId: PersonId): BotState
    suspend fun set(botId: BotId, userId: PersonId, botState: BotState)
    suspend fun allStates(botId: BotId):List<BotState>
}
