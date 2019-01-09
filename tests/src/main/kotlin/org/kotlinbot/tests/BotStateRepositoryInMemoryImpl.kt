package org.kotlinbot.tests

import org.kotlinbot.api.BotId
import org.kotlinbot.api.BotState
import org.kotlinbot.api.IntentId
import org.kotlinbot.api.inevents.PersonId
import org.kotlinbot.core.BotStateRepository
import java.util.concurrent.ConcurrentHashMap

class BotStateRepositoryInMemoryImpl : BotStateRepository {
    val values = ConcurrentHashMap<Pair<BotId, PersonId>, BotState>()
    override suspend fun get(botId: BotId, userId: PersonId): BotState {
        return values[botId to userId] ?: BotState(
            botId = botId,
            userId = userId
        )
    }

    override suspend fun set(botId: IntentId, userId: PersonId, botState: BotState) {
        values[botId to userId] = botState
    }

    override suspend fun allStates(botId: BotId): List<BotState> {
        return values.filterKeys { it.first == botId }.values.toList()
    }
}