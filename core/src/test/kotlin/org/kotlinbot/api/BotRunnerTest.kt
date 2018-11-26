package org.kotlinbot.api

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.kotlinbot.api.Intent.Companion.fromHandler
import org.kotlinbot.api.IntentActivator.Companion.anyOf
import org.kotlinbot.api.IntentActivator.Companion.onText
import org.kotlinbot.api.IntentActivator.Companion.startIntent
import org.kotlinbot.api.IntentEventHandler.Companion.intentHandlerOf
import org.kotlinbot.api.inevents.*
import org.kotlinbot.api.outevents.CallbackReply
import org.kotlinbot.core.BotShell
import org.kotlinbot.tests.TestBotRunnerWrapper
import org.kotlinbot.tests.testItem

internal val intent1 = CommonScope::handler1.name
internal val intent2 = CommonScope::handler2.name
internal val intent3 = Handler3Scope::handler3.name
internal val intent4 = CommonScope::handler4.name

class BotRunnerTest {

    private lateinit var runner: TestBotRunnerWrapper

    @Before
    fun init() {
        val botShell = BotShell(
            handlers = assembleBotHandlers()
        )

        runner = TestBotRunnerWrapper(botShell)
    }

    @Test
    fun `activate first intent on clean start`() = runBlocking {
        runner("hi")
        val intentScope = runner.intentScope<CommonScope>(intent1)
        assertTrue(intentScope.handler1Done)
        assertFalse(intentScope.handler2Done)
    }

    @Test
    fun `changes made before start another intent store into scope`() = runBlocking {
        runner("2")
        runner("finish")
        val intentScope = runner.intentScope<CommonScope>(intent1)
        assertTrue(intentScope.handler2BeforeFinish)
    }

    @Test
    fun `changes made before finish intent store into scope`() = runBlocking {
        runner("2")
        val intentScope = runner.intentScope<CommonScope>(intent1)
        assertTrue(intentScope.handler1Done)
    }

    @Test
    fun `start another intent inside active and use  onEvent handler when onstart is missing`() = runBlocking {
        runner("2")
        val intentScope = runner.intentScope<CommonScope>(intent1)
        assertTrue(intentScope.handler1Done)
        assertTrue(intentScope.handler2Done)
        assertEquals("2", intentScope.handler2startEventText)
        assertFalse(intentScope.handler1AfterJump)
    }

    @Test
    fun `run on return handler when return from started intent`() = runBlocking {
        runner("2")
        val intentScope1 = runner.intentScope<CommonScope>(intent1)
        assertFalse(intentScope1.handler1Return)
        runner("finish")
        val intentScope2 = runner.intentScope<CommonScope>(intent1)
        assertTrue(intentScope2.handler1Return)
    }

    @Test
    fun `when intent has otherwise method call - called intent with match activator`() = runBlocking {
        runner("intent3Simpl")
        val intentScope3 = runner.intentScope<CommonScope>(intent3)
        assertTrue(intentScope3.handler3Called)
        assertEquals(intent1, runner.activeIntentId)
    }

    @Test
    fun `when intent has otherwise method call - start match activator intent `() = runBlocking {
        runner("intent3Start")
        val intentScope3 = runner.intentScope<CommonScope>(intent3)
        assertTrue(intentScope3.handler3Called)
        assertEquals(intent3, runner.activeIntentId)
    }

    @Test
    fun `when intent has otherwise method call - check otherwise in intent activation order`() = runBlocking {
        runner("intent3Simple")
        runner("4")
        assertEquals(intent3, runner.activeIntentId)
        runner("finish")
        runner("intent4")
        assertEquals(intent4, runner.activeIntentId)
        runner("finish")
        runner("4")
        assertEquals(
            "Must activate handler4 cause it was last active before otherwise",
            intent4, runner.activeIntentId
        )
    }

    @Test
    fun `when intent has otherwise method call but no other match - call inside otherwise block`() = runBlocking {
        runner("random doesnt match text")
        val intentScope1 = runner.intentScope<CommonScope>(intent1)
        assertTrue("If no other intent called - must call otherwise block", intentScope1.handler1OtherwiseCalled)
    }

    @Test
    fun `when we start another intent and it finished - call onReturn handler`() = runBlocking {
        runner("2")
        runner("finish")
        val intentScope1 = runner.intentScope<CommonScope>(intent1)
        assertTrue("On return must be called", intentScope1.handler1Return)
    }

    @Test
    fun `when another intent started throught overwise logic - onInterrupt must be called on active intent`() =
        runBlocking {
            runner("3")
            runner("interrupt")
            assertTrue("OnInterrupt must be called", runner.intentScope<CommonScope>(intent3).handler3Interrupted)
        }

    @Test
    fun `when another intent started throught overwise logic finished - onResume must be called on previouseActive intent`() =
        runBlocking {
            runner("2")
            runner("finish")
            val intentScope1 = runner.intentScope<CommonScope>(intent1)
            assertTrue("OnReturn must be called", intentScope1.handler1Return)
        }

    @Test
    fun `when intent finished - its scope data must be removed`() =
        runBlocking {
            runner("3")
            val scopeBefore = runner.intentScope<Handler3Scope>(intent3)
            assertTrue("Value seted by handler", scopeBefore.handler3private)
            runner("finish")
            val scopeAfter = runner.intentScope<Handler3Scope>(intent3)
            assertFalse("Value must be removed", scopeAfter.handler3private)


        }

    @Test
    fun `callback enevts must be handled throught onCallback`() =
        runBlocking {
            runner(Callback(MessageId.testItem(), userId = UserId.testItem(), stringData = "handler1|1", native = ""))
            val scope = runner.intentScope<CommonScope>(intent1)
            assertTrue("onCallback must be called", scope.handler1CallbackCalled)
        }

    @Test
    fun `when we receive callback and dont reply it explict - must reply implict`() =
        runBlocking {
            runner(Callback(MessageId.testItem(), userId = UserId.testItem(), stringData = "handler1|2", native = ""))
            assertTrue("Must send callback reply", runner.messages.any { it is CallbackReply })
        }

    @Test
    fun `when we receive callback and  reply it explict - must not send second reply`() =
        runBlocking {
            runner(Callback(MessageId.testItem(), userId = UserId.testItem(), stringData = "handler1|3", native = ""))
            assertEquals("Must send only one callback reply", 1, runner.messages.count { it is CallbackReply })
        }

}

private fun assembleBotHandlers(): List<IntentEventHandler> {
    return listOf(
        intentHandlerOf(
            intent1, fromHandler(
                onEvent = CommonScope::handler1,
                onReturn = { handler1Return = true },
                onCallback = { handler1CallbackCalled = true }
            ), "handler1"
        ),
        intentHandlerOf(intent2, CommonScope::handler2),
        intentHandlerOf(
            intent3, fromHandler(
                onEvent = Handler3Scope::handler3,
                onElect = anyOf(
                    onText("intent3Simpl", block = Handler3Scope::handler3),
                    onText("intent3Start", block = startIntent()),
                    onText("4", block = startIntent())
                ),
                onInterrupt = { handler3Interrupted = true }
            )
        ),
        intentHandlerOf(
            intent4, fromHandler(
                onEvent = CommonScope::handler4,
                onElect = anyOf(
                    onText("4", block = startIntent()),
                    onText("interrupt", block = startIntent())
                )
            )
        )


    )
}

private interface CommonScope : BotScope {
    var handler1Done: Boolean
    var handler1AfterJump: Boolean
    var handler1OtherwiseCalled: Boolean
    var handler1Return: Boolean
    var handler1CallbackCalled: Boolean


    var handler2Done: Boolean
    var handler2startEventText: String
    var handler2BeforeFinish: Boolean


    var handler3Called: Boolean
    var handler3Interrupted: Boolean

    var handler4CalledTimes: Int

}

private interface Handler3Scope : CommonScope {
    var handler3private: Boolean
}

private suspend fun CommonScope.handler1(e: InEvent) {
    handler1Done = true
    if (e is Text) {
        when (e.message) {
            "2" -> {
                start(intent2);handler1AfterJump = true
            }
            "3" -> {
                start(intent3);handler1AfterJump = true
            }
            "intent4" -> {
                start(intent4);handler1AfterJump = true
            }
        }
    }

    otherwise {
        handler1OtherwiseCalled = true
    }
}

private suspend fun CommonScope.handler2(e: InEvent) {
    handler2Done = true
    handler2startEventText = (e as Text).message
    if (e.message == "finish") {
        handler2BeforeFinish = true
        finish()
    }
}

private suspend fun Handler3Scope.handler3(e: InEvent) {
    handler3Called = true
    handler3private = true
    if (e is Text && e.message == "finish")
        finish()
    otherwise()
}

private suspend fun CommonScope.handler4(e: InEvent) {
    handler4CalledTimes++
    if (e is Text && e.message == "finish")
        finish()
}