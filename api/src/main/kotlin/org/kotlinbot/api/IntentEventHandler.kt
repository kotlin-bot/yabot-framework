package org.kotlinbot.api

import java.util.concurrent.atomic.AtomicInteger

data class IntentEventHandler(
    val intentId: IntentId,
    val scopeClasss: Class<out BotScope>,
    val intent: Intent<BotScope>,
    val callbackPrefixes: Set<String> = emptySet()
) {
    companion object {
        val idxCounter = AtomicInteger(1)

        inline fun <reified T : BotScope> intentHandlerOf(
            intentName: String,
            noinline handler: EventHandler<T>,
            vararg callbackPrefixers: String
        ): IntentEventHandler {

            return IntentEventHandler(
                intentId = intentName,
                scopeClasss = T::class.java,
                intent = Intent.fromHandler(onEvent = handler) as Intent<BotScope>,
                callbackPrefixes = callbackPrefixers.toSet()
            )
        }

        inline fun <reified T : BotScope> intentHandlerOf(
            noinline handler: EventHandler<T>
        ): IntentEventHandler =
            intentHandlerOf(
                intentName = "nonameIntent${idxCounter.getAndIncrement()}",
                handler = handler
            )

        inline fun <reified T : BotScope> intentHandlerOf(
            intentName: String = "nonameIntent${idxCounter.getAndIncrement()}",
            intent: Intent<T>,
            vararg callbackPrefixers: String
        ): IntentEventHandler {
            return IntentEventHandler(
                intentId = intentName,
                scopeClasss = T::class.java,
                intent = intent as Intent<BotScope>,
                callbackPrefixes = callbackPrefixers.toSet()
            )
        }
    }
}