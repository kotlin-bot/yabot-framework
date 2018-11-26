package org.kotlinbot.runner

import com.mongodb.async.client.MongoDatabase
import kotlinx.coroutines.runBlocking
import org.kotlinbot.api.BotId
import org.kotlinbot.api.BotState
import org.kotlinbot.api.inevents.UserId
import org.kotlinbot.core.BotStateRepository
import org.litote.kmongo.and
import org.litote.kmongo.async.getCollection
import org.litote.kmongo.coroutine.ensureUniqueIndex
import org.litote.kmongo.coroutine.findOne
import org.litote.kmongo.coroutine.replaceOne
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.eq

class BotStateRepositoryMongoImpl(db: MongoDatabase) : BotStateRepository {
    val col = db.getCollection<BotState>()

    init {
        runBlocking {
            col.ensureUniqueIndex(BotState::botId, BotState::userId)
        }
    }

    override suspend fun get(botId: BotId, userId: UserId): BotState {
        return col.findOne(and(BotState::botId eq botId, BotState::userId eq userId)) ?: BotState(
            botId,
            userId
        )
    }

    override suspend fun set(botId: BotId, userId: UserId, botState: BotState) {
        col.replaceOne(and(BotState::botId eq botId, BotState::userId eq userId), botState)
    }

    override suspend fun allStates(botId: BotId): List<BotState> {
        return col.find(BotState::botId eq botId).toList()
    }
}