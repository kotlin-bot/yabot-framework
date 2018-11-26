package org.kotlinbot.core

import org.kotlinbot.api.BotScope
import org.kotlinbot.api.EventHandler
import org.kotlinbot.api.Intent

internal enum class CallMethod {
    ON_START {
        override suspend fun <T : BotScope> intentMethod(intent: Intent<T>): EventHandler<T>? {
            return intent.onStart
        }
    },
    ON_EVENT {
        override suspend fun <T : BotScope> intentMethod(intent: Intent<T>): EventHandler<T>? {
            return intent.onEvent
        }

    },
    ON_CALLBACK {
        override suspend fun <T : BotScope> intentMethod(intent: Intent<T>): EventHandler<T>? {
            return intent.onCallback as EventHandler<T>
        }

    },
    ON_INTERRUPT {
        override suspend fun <T : BotScope> intentMethod(intent: Intent<T>): EventHandler<T>? {
            return intent.onInterrupt
        }

    },
    ON_RETURN {
        override suspend fun <T : BotScope> intentMethod(intent: Intent<T>): EventHandler<T>? {
            return intent.onReturn
        }

    };

    abstract suspend fun <T : BotScope> intentMethod(intent: Intent<T>): EventHandler<T>?
}