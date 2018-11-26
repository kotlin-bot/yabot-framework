package org.kotlinbot.api

import org.kotlinbot.api.inevents.InEvent
import org.kotlinbot.api.inevents.Text

interface IntentActivator<T : BotScope> {
    suspend operator fun T.invoke(event: InEvent): EventHandler<T>?

    companion object {

        fun <T : BotScope> noActivator(): IntentActivator<T> {
            return object : IntentActivator<T> {
                override suspend fun T.invoke(event: InEvent): EventHandler<T>? = null
            }
        }

        fun <T : BotScope> anyOf(vararg activators: IntentActivator<T>): IntentActivator<T> {
            return object : IntentActivator<T> {
                override suspend fun T.invoke(event: InEvent): EventHandler<T>? {
                    for (condition in activators) {
                        val handler = with(condition) { this@invoke.invoke(event) }
                        if (handler != null)
                            return handler
                    }
                    return null
                }
            }
        }

        fun <T : BotScope> onText(vararg string: String, block: EventHandler<T>) =
            process({ event ->
                event is Text && string.any {
                    event.message.contains(
                        it,
                        true
                    )
                }
            }, block)


        fun <T : BotScope> startIntent(intentId: IntentId? = null): EventHandler<T> {
            return { start(intentId ?: selfIntentId, true) }
        }


        private fun <T : BotScope> process(
            condition: suspend T.(event: InEvent) -> Boolean,
            block: EventHandler<T>
        ): IntentActivator<T> {
            return object : IntentActivator<T> {
                override suspend fun T.invoke(event: InEvent) =
                    if (this.condition(event)) block else null
            }
        }
    }
}

