package org.kotlinbot.core.platform.scope

import org.kotlinbot.api.*
import org.kotlinbot.api.inevents.ChatId
import org.kotlinbot.api.inevents.MessageId
import org.kotlinbot.api.inevents.UserId
import org.kotlinbot.api.methods.AttachmentResolver
import org.kotlinbot.api.methods.DispatchException
import org.kotlinbot.api.methods.ReplyMethods
import org.kotlinbot.api.methods.ReplyNowMethods
import org.kotlinbot.core.ServiceRegistry
import org.kotlinbot.core.platform.ReplyNowMethodsImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

data class CallContext(
    override val botId: BotId,
    override val chatId: ChatId,
    override val userId: UserId,
    override val profile: UserProfile,
    override val messageId: MessageId?,
    override val selfIntentId: IntentId,
    private val serviceRegistry: ServiceRegistry,
    private val otherwiseHandler: suspend (block: suspend () -> Unit) -> Unit
) : BotScope,
    AttachmentResolver by serviceRegistry[AttachmentResolver::class.java],
    ReplyMethods by serviceRegistry[ReplyMethods::class.java],
    ReplyNowMethods by ReplyNowMethodsImpl(
        chatId,
        messageId,
        serviceRegistry[ReplyService::class.java]
    ) {

    val otherWiseCalled = AtomicBoolean()

    override suspend fun otherwise(block: suspend () -> Unit) {
        if (!otherWiseCalled.getAndSet(true)) {
            try {
                otherwiseHandler(block)
            } catch (e: DispatchException) {
                logger.warn("override start logic inside otherwise")
                if (e.startIntentId != null)
                    throw e.copy(interrupt = true)
                else
                    throw e
            }
        } else {
            logger.warn("Recursive otherwise calling at intent {}. Dont ask other intents", selfIntentId)
            block()
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(CallContext::class.java)
    }
}




