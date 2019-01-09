package org.kotlinbot.tests

import kotlinx.coroutines.runBlocking
import org.kotlinbot.api.*
import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.inevents.PersonId
import org.kotlinbot.api.inevents.Text
import org.kotlinbot.api.inevents.TextKind
import org.kotlinbot.api.outevents.TextMessage
import org.kotlinbot.core.BotRunner
import org.kotlinbot.core.BotShell
import org.kotlinbot.core.BotStateRepository
import java.util.concurrent.atomic.AtomicBoolean


class TestBotRunnerWrapper(
    botShell: BotShell,
    botStateRepository: BotStateRepository = BotStateRepositoryInMemoryImpl()
) :
    BotRunner(botShell = botShell, botStateRepository = botStateRepository) {

    private val replyService = DummyReplyService()

    init {
        regitsterService(ReplyService::class.java, replyService)
    }

    suspend operator fun invoke(message: String) {
        this(text(message))
    }

    suspend fun <S : BotScope> intentScope(intentId: IntentId, userId: PersonId = PersonId.testItem()): S {

        val botState = getBotStateForUser(userId)
        return createDynamicScope<S>(
            botId = ONLY_BOT,
            userId = userId,
            chatId = userId,
            personProfile = botState.personProfile,
            selfIntentId = intentId,
            activeIntentId = intentId,
            values = botState.intentState(intentId),
            otherwiseHandler = {},
            otherwiseWasCalled = AtomicBoolean(false)
        ).asScope()
    }

    suspend fun dumpCommonState(userId: PersonId = PersonId.testItem()): Map<String, Any?> {
        return botStateRepository.get(ONLY_BOT, userId).commonState
    }

    val messages get() = replyService.messages
    val botStates: List<BotState>
        get() = runBlocking {
            botStateRepository.allStates(ONLY_BOT)
        }
    val activeIntentId: IntentId?
        get () = runBlocking {
            getBotStateForUser(PersonId.testItem()).activeIntentId()
        }

    fun fetchMessagesText(): List<String> {
        val result = messages.filterIsInstance(TextMessage::class.java).map { it.text }
        messages.clear()
        return result
    }
}

fun text(str: String, userId: PersonId = PersonId.testItem()) = Text(
    personId = userId,
    message = str,
    chatId = TestChatId(),
    kind = TextKind.UNKNOWN,
    messageId = MessageId.testItem(),
    native = ""
)