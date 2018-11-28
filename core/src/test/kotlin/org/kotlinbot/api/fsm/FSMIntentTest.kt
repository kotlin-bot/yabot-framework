package org.kotlinbot.api.fsm

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.kotlinbot.api.BotScope
import org.kotlinbot.api.Intent.Companion.fromHandler
import org.kotlinbot.api.IntentEventHandler
import org.kotlinbot.api.inevents.InEvent
import org.kotlinbot.api.outevents.TextMessage
import org.kotlinbot.api.replyRouter
import org.kotlinbot.core.BotShell
import org.kotlinbot.tests.TestBotRunnerWrapper

internal val intent1 = CommonScope::handler1.name
internal val fsmIntentId = "fsmIntentId"

class FSMIntentTest {
    private lateinit var runner: TestBotRunnerWrapper

    @Before
    fun init() {
        val botShell = BotShell(
            handlers = assembleBotHandlers()
        )

        runner = TestBotRunnerWrapper(botShell)
    }

    @Test
    fun smokeTests() = runBlocking {
        runner("1")
        assertEquals(intent1, runner.activeIntentId)
        assertEquals(1, runner.messages.size)
        assertEquals("1", (runner.messages.first() as TextMessage).text)

        runner("2")
        assertEquals(fsmIntentId, runner.activeIntentId)
        assertEquals(3, runner.messages.size)
        assertEquals(
            listOf("1", "2", "how are you?"),
            runner.messages
                .filterIsInstance(TextMessage::class.java)
                .map { it.text }
        )

        var fsmScope: TestFSMIntentScope = runner.intentScope(fsmIntentId)
        assertEquals("StartState", fsmScope.activeStateId)

        runner("not so good")
        fsmScope = runner.intentScope(fsmIntentId)
        assertEquals("StartState", fsmScope.activeStateId)

        runner("fine")
        fsmScope = runner.intentScope(fsmIntentId)
        assertEquals("MiddleState", fsmScope.activeStateId)

        runner("reply2")
        fsmScope = runner.intentScope(fsmIntentId)
        assertEquals("FinalState", fsmScope.activeStateId)

        runner("reply2")
        assertEquals(intent1, runner.activeIntentId)


    }

    private fun assembleBotHandlers(): List<IntentEventHandler> {
        return listOf(
            IntentEventHandler.intentHandlerOf(
                intent1, fromHandler(onEvent = CommonScope::handler1)
            ),
            IntentEventHandler.intentHandlerOf(
                fsmIntentId, FSMIntent(listOf(StartState(), MiddleState(), FinalState()))
            )
        )
    }
}

private interface CommonScope : BotScope

private suspend fun CommonScope.handler1(e: InEvent) = with(replyRouter(this, e)) {
    onText("1") { _, _ ->
        reply("1")
    }
    onText("2") { _, _ ->
        reply("2")
        start(fsmIntentId)
    }
    otherwise {

    }
}

private interface TestFSMIntentScope : FSMIntentScope, CommonScope {
    var startStateAsked: Boolean
    var startStateHandler: Boolean
}

private class StartState : FSMState<TestFSMIntentScope> {
    override suspend fun TestFSMIntentScope.ask(event: InEvent?) {
        reply("how are you?")
    }

    override suspend fun TestFSMIntentScope.handleReply(event: InEvent) = with(replyRouter(this, event)) {
        onText("fine") { _, _ ->
            reply("nice to hear")
            goToState(States.MIDDLE)
        }
        onText("not so good") { _, _ ->
            reply("whatswrong?")
        }
        otherwise()
    }
}

private class MiddleState : FSMState<TestFSMIntentScope> {
    override suspend fun TestFSMIntentScope.ask(event: InEvent?) {
        reply("Ask message")
    }

    override suspend fun TestFSMIntentScope.handleReply(event: InEvent) = with(replyRouter(this, event)) {
        onText("reply1") { _, _ ->
            reply("reply1")
        }
        onText("reply2") { _, _ ->
            reply("reply2")
            goToState(States.FINAL)
        }
        otherwise()
    }
}

private class FinalState : FSMState<TestFSMIntentScope> {
    override suspend fun TestFSMIntentScope.ask(event: InEvent?) {
        reply("Ask final message")
    }

    override suspend fun TestFSMIntentScope.handleReply(event: InEvent) = with(replyRouter(this, event)) {
        onText("reply1") { _, _ ->
            reply("reply1")
        }
        onText("reply2") { _, _ ->
            reply("reply2")
            finish()
        }
        otherwise()
    }
}

enum class States(val state: FSMState<TestFSMIntentScope>) : FSMState<TestFSMIntentScope> by state {
    START(StartState()), MIDDLE(MiddleState()), FINAL(FinalState())
}