package org.kotlinbot.api

import org.kotlinbot.api.inevents.InEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface InEventConsumer {
    suspend operator fun invoke(event: InEvent)

    companion object {
        val logger: Logger =
            LoggerFactory.getLogger(InEventConsumer::class.java)
    }
}

fun InEventConsumer.handleExceptions(): InEventConsumer {

    return object : InEventConsumer {
        override suspend fun invoke(event: InEvent) {
            try {
                this@handleExceptions(event)
            } catch (e: Exception) {
                InEventConsumer.logger.warn("Failed to process event {} cause intentHandlerOf exception", event, e)
            }
        }
    }
}