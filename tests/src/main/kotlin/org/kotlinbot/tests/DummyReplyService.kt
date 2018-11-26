package org.kotlinbot.tests

import org.kotlinbot.api.ReplyService
import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.inevents.Origin
import org.kotlinbot.api.outevents.OutEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DummyReplyService : ReplyService {
    val messages = ArrayList<OutEvent>()

    override suspend fun invoke(outEvent: OutEvent): MessageId {
        messages.add(outEvent)
        return DummyMessageId
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(DummyReplyService::class.java)
    }
}

internal object DummyMessageId : MessageId {
    override val origin: Origin
        get() = Origin.TEXT
}