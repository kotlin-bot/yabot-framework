package org.kotlinbot.tests


import org.kotlinbot.api.BotScope
import org.kotlinbot.api.ONLY_BOT
import org.kotlinbot.api.PersonProfile
import org.kotlinbot.api.ReplyService
import org.kotlinbot.api.inevents.*
import org.kotlinbot.core.ServiceRegistry
import org.kotlinbot.core.platform.scope.CallContext
import org.kotlinbot.core.platform.scope.DynamicScope
import org.kotlinbot.core.platform.scope.ScopeFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

//const val TEST_BOT_ID_1: BotId = "test-bot-1"
//const val TEST_BOT_ID_2: BotId = "test-bot-2"

data class TestMessageId(val id: Int = Random.nextInt()) : MessageId {
    override val origin: Origin
        get() = Origin.TEXT
}

fun MessageId.Companion.testItem() = TestMessageId()

data class TestUserId(override val kind: UserKind, val id: Int = 1) : PersonId {
    override val origin: Origin
        get() = Origin.TEXT
}

fun PersonId.Companion.testItem(kind: UserKind = UserKind.HUMAN) = TestUserId(kind)

data class TestChatId(val id: Int = 2) : ChatId {
    override val origin: Origin
        get() = Origin.TEXT
}

fun ChatId.Companion.testItem() = TestChatId()

inline fun <reified SCOPE : BotScope> scopeFor(
    values: Map<String, Any?> = emptyMap(),
    serviceRegistry: ServiceRegistry = ServiceRegistry()
): Pair<SCOPE, ServiceRegistry> {

    val (scope, registry) = dynamicScopeFor<SCOPE>(values, serviceRegistry)
    return scope.asScope() to registry
}

inline fun <reified SCOPE : BotScope> dynamicScopeFor(
    values: Map<String, Any?> = emptyMap(),
    serviceRegistry: ServiceRegistry = ServiceRegistry()
): Pair<DynamicScope<SCOPE>, ServiceRegistry> {

    serviceRegistry
        .registerIfAbsent(ReplyService::class.java, DummyReplyService())

    val s = ScopeFactory()
    val callContext = CallContext(
        botId = ONLY_BOT,
        chatId = ChatId.testItem(),
        userId = PersonId.testItem(),
        profile = PersonProfile(PersonId.testItem()),
        messageId = MessageId.testItem(),
        selfIntentId = "TestIntent1",
        serviceRegistry = serviceRegistry,
        otherwiseHandler = {},
        activeIntentId = "TestIntent1",
        otherWiseCalled = AtomicBoolean(false)
    )

    return s.createDynamicScope(SCOPE::class, callContext, serviceRegistry, values) to serviceRegistry
}