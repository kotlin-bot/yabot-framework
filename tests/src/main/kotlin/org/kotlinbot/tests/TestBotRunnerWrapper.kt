package org.kotlinbot.tests

import kotlinx.coroutines.runBlocking
import org.kotlinbot.api.*
import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.inevents.Text
import org.kotlinbot.api.inevents.TextKind
import org.kotlinbot.api.inevents.UserId
import org.kotlinbot.core.BotRunner
import org.kotlinbot.core.BotShell


class TestBotRunnerWrapper(botShell: BotShell) :
    BotRunner(botShell = botShell, botStateRepository = BotStateRepositoryInMemoryImpl()) {

    private val replyService = DummyReplyService()

    init {
        regitsterService(ReplyService::class.java, replyService)
    }

    suspend operator fun invoke(message: String) {
        this(text(message))
    }

    suspend fun <S : BotScope> intentScope(intentId: IntentId, userId: UserId = UserId.testItem()): S {

        val botState = getBotStateForUser(userId)
        return createDynamicScope<S>(
            botId = ONLY_BOT,
            userId = userId,
            chatId = userId,
            userProfile = botState.userProfile,
            intentId = intentId,
            values = botState.intentState(intentId),
            otherwiseHandler = {}
        ).asScope()
    }

    suspend fun dumpCommonState(userId: UserId = UserId.testItem()): Map<String, Any?> {
        return botStateRepository.get(ONLY_BOT, userId).commonState
    }

    val messages get() = replyService.messages
    val botStates: List<BotState>
        get() = runBlocking {
            botStateRepository.allStates(ONLY_BOT)
        }
    val activeIntentId: IntentId?
        get () = runBlocking {
            getBotStateForUser(UserId.testItem()).activeIntentId()
        }
}

fun text(str: String, userId: UserId = UserId.testItem()) = Text(
    userId = userId,
    message = str,
    chatId = TestChatId(),
    kind = TextKind.UNKNOWN,
    messageId = MessageId.testItem(),
    native = ""
)