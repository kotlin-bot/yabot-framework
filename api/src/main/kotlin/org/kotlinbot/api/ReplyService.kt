package org.kotlinbot.api

import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.inevents.Origin
import org.kotlinbot.api.outevents.OutEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.SocketException
import java.net.SocketTimeoutException

interface ReplyService {
    suspend operator fun invoke(outEvent: OutEvent): MessageId?


    fun retryOnIoErrors(times: Int = 5): ReplyService {
        return object : ReplyService {
            override suspend fun invoke(outEvent: OutEvent) = delegateCall(outEvent)

            suspend fun delegateCall(event: OutEvent, tryCount: Int = 0): MessageId? {
                if (tryCount < times)
                    try {
                        return this@ReplyService.invoke(event)
                    } catch (e: SocketException) {
                        logger.warn("Socket problem sending events to {}. Try {}", event.origin, tryCount)
                        return delegateCall(event, tryCount + 1)
                    } catch (e: SocketTimeoutException) {
                        logger.warn("Socket  timeout problem sending events to {}. Try {}", event.origin, tryCount)
                        return delegateCall(event, tryCount + 1)
                    }
                else {
                    logger.warn("Timeout on sending events to {}. Giveup", event.origin)
                    return null
                }
            }
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ReplyService::class.java)
    }
}

data class RouteReplyService(val transports: Map<Origin, ReplyService>) :
    ReplyService {
    override suspend fun invoke(outEvent: OutEvent): MessageId? {
        val nativeTransport = transports[outEvent.origin] ?: error("cant find transport match event")
        return nativeTransport(outEvent)
    }
}