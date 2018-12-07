package org.kotlinbot.core

import org.kotlinbot.api.*
import org.kotlinbot.api.ReplyService
import org.kotlinbot.api.inevents.*
import org.kotlinbot.api.methods.DispatchException
import org.kotlinbot.api.methods.ReplyMethods
import org.kotlinbot.api.outevents.CallbackReply
import org.kotlinbot.core.CallMethod.*
import org.kotlinbot.core.platform.ReplyMethodsImpl
import org.kotlinbot.core.platform.scope.CallContext
import org.kotlinbot.core.platform.scope.DynamicScope
import org.kotlinbot.core.platform.scope.ScopeFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

open class BotRunner(
    val botShell: BotShell,
    val botStateRepository: BotStateRepository,
    val profileResolver: ProfileResolver = ProfileResolverRouter(emptyMap())
) : InEventConsumer {
    val commonServiceRegistry: ServiceRegistry = ServiceRegistry()

    val scopeFactory = ScopeFactory()

    override suspend fun invoke(event: InEvent) {
        val messageId = (event as? InMessage)?.messageId
        val chatId = event.chatId
        val userId = event.userId
        val replyStore = ReplyMethodsImpl(
            chatId,
            messageId
        )
        val joinedServiceRegistry = botShell.serviceRegistry.copy(
            registry = HashMap(botShell.serviceRegistry.registry),
            parent = commonServiceRegistry
        )
        joinedServiceRegistry[ReplyMethods::class.java] = replyStore
        val botState: BotState = resolveUserProfileIfNeed(getBotStateForUser(userId), event)

        logger.debug("Call stack before: {}", botState.callStack)
        logger.debug("Activity history before: {}", botState.activityHistory)
        val (method, intentId: IntentId?) = if (event is Callback) {
            botShell.handlers
                .firstOrNull { it.callbackPrefixes.contains(event.stringData.substringBefore("|")) }
                ?.let { ON_CALLBACK to it.intentId } ?: null to null
        } else {
            val method = if (botState.callStack.isEmpty()) {
                botState.startIntent(botShell.handlers.first().intentId)
                ON_START
            } else {
                CallMethod.ON_EVENT
            }

            method to botState.activeIntentId()
        }

        if (intentId != null && method != null) {

            val callArguments = CallArguments(
                event = event,
                serviceRegistry = joinedServiceRegistry,
                botState = botState,
                activeIntentId = intentId
            )

            botState.intentWasActivated(intentId)
            executeCall(
                intentId,
                callArguments,
                callMethod(method, intentId)
            )
        } else {
            logger.warn("no handler found to handler {}", event)
        }

        val outMessages = replyStore.eventsQueue
        val replyService = commonServiceRegistry[ReplyService::class.java]
        if (event is Callback && !outMessages.any { it is CallbackReply }) {
            outMessages.add(0, CallbackReply(chatId = event.chatId, replyId = event.messageId))
        }

        outMessages.forEach { msg ->
            replyService(msg)
        }

        botStateRepository.set(ONLY_BOT, userId, botState)

        logger.debug("Call stack after: {}", botState.callStack)
        logger.debug("Activity history after: {}", botState.activityHistory)
    }

    private suspend fun resolveUserProfileIfNeed(botState: BotState, event: InEvent): BotState {
        if (botState.userProfile.resolved)
            return botState
        return profileResolver.resolveProfileFromEvent(event)?.let {
            botState.copy(userProfile = it)
        } ?: botState
    }

    private suspend fun executeCall(
        selfIntentId: IntentId,
        callArguments: CallArguments,
        methodSource: suspend (botScope: BotScope) -> EventHandler<BotScope>?,
        otherwiseHandler: suspend (block: suspend () -> Unit) -> Unit = createOtherwiseHandler(
            callArguments
        )
    ): Boolean {

        val botState = callArguments.botState

        val scope: DynamicScope<out BotScope> =
            createDynamicScope(
                botId = botState.botId,
                chatId = callArguments.chatId,
                userId = callArguments.userId,
                userProfile = botState.userProfile,
                messageId = callArguments.messageId,
                selfIntentId = selfIntentId,
                activeIntentId = callArguments.activeIntentId,
                joinedServiceRegistry = callArguments.serviceRegistry,
                values = botState.intentState(selfIntentId),
                otherwiseHandler = otherwiseHandler
            )
        try {

            val botScope = scope.asScope()
            val method = methodSource(botScope)
            return if (method != null) {
                method.invoke(botScope, callArguments.event)
                applyScopeChanges(botState, scope.values, selfIntentId)
                true
            } else {
                false
            }
        } catch (e: DispatchException) {
            logger.debug("change call stack stert: ${e.startIntentId} - finish: ${e.finishIntentId}, interrupt: ${e.interrupt}")
            if (e.startIntentId == botState.activeIntentId()) {
                logger.warn("You try to start intent from intent itself ${e.startIntentId}. Skip it")
                return true
            }
            if (e.interrupt) {
                while (botState.intentToInterruptLeft()) {
                    val intentIdToInterrupt = botState.activeIntentId()
                    callIntentMethod(intentIdToInterrupt, ON_INTERRUPT, callArguments)
                    applyScopeChanges(botState, scope.values, intentIdToInterrupt)
                    botState.finishActiveIntent(true)
                }
                botState.startIntent(e.startIntentId!!)
                callIntentMethod(e.startIntentId!!, ON_START, callArguments)

            } else {
                if (e.finishIntentId != null && e.finishIntentId != botShell.handlers.first().intentId) {
                    botState.finishActiveIntent()
                    applyScopeChanges(botState, scope.values)
                } else {
                    applyScopeChanges(botState, scope.values, selfIntentId)
                }


                if (e.startIntentId != null) {
                    botState.startIntent(e.startIntentId!!)
                    callIntentMethod(e.startIntentId!!, ON_START, callArguments)
                } else {
                    callIntentMethod(botState.activeIntentId(), ON_RETURN, callArguments)

                }
            }
            return true
        }
    }

    private suspend fun callIntentMethod(
        intentId: IntentId,
        method: CallMethod,
        callArguments: CallArguments
    ) {
        val intentMethod = callMethod(method, intentId)
        executeCall(
            intentId,
            callArguments,
            intentMethod
        )
    }

    private suspend fun electMethod(
        intentId: IntentId,
        event: InEvent
    ): suspend (botScope: BotScope) -> EventHandler<BotScope>? {
        return { botScope: BotScope -> with(getIntentById(intentId).intent.onElect!!) { botScope.invoke(event) } }
    }

    private suspend fun callMethod(
        method: CallMethod,
        intentId: IntentId
    ): suspend (botScope: BotScope) -> EventHandler<BotScope>? {
        return { method.intentMethod(getIntentById(intentId).intent) }
    }

    private fun createRejectOtherwiseHandler(): suspend (block: suspend () -> Unit) -> Unit {
        return { block: suspend () -> Unit ->
            logger.warn("Dont try to call otherwise chain from inside otherwise chain")
        }
    }

    private fun createOtherwiseHandler(
        callArguments: CallArguments
    ): suspend (suspend () -> Unit) -> Unit {
        return { block: suspend () -> Unit ->
            val intentToActivate: IntentEventHandler? = botShell
                .handlers
                .sortedBy {
                    otherwiseCheckOrderForIntetnHandler(callArguments.botState, it)
                }
                .firstOrNull { handler ->
                    if (handler.intent.onElect == null)
                        return@firstOrNull false

                    executeCall(
                        selfIntentId = handler.intentId,
                        callArguments = callArguments,
                        methodSource = electMethod(handler.intentId, callArguments.event),
                        otherwiseHandler = createRejectOtherwiseHandler()
                    )
                }
            if (intentToActivate == null) {
                block()
            } else {
                callArguments.botState.intentWasActivated(intentToActivate.intentId)
            }

        }
    }

    private fun otherwiseCheckOrderForIntetnHandler(
        botState: BotState,
        intentEventHandler: IntentEventHandler
    ): Int {
        val activityIdx = botState.activityHistory.indexOf(intentEventHandler.intentId)
        return if (activityIdx >= 0)
            activityIdx
        else
            botShell.handlers.indexOf(intentEventHandler)
    }


    private fun applyScopeChanges(
        botState: BotState,
        newValues: MutableMap<String, Any?>,
        intentId: IntentId? = null
    ) {
        val (newBotScope, newIntentScope) = botShell.splitPropsMap(newValues)
        //botState.commonState.clear()
        botState.commonState.putAll(newBotScope)
        if (intentId != null)
            botState.setIntentState(intentId, newIntentScope)
    }

    internal fun getIntentById(intentId: IntentId) = botShell.handlers.find { it.intentId == intentId }!!

    protected suspend fun getBotStateForUser(userId: UserId) = botStateRepository.get(ONLY_BOT, userId)

    protected fun <T : BotScope> createDynamicScope(
        botId: BotId,
        chatId: ChatId,
        userId: UserId,
        userProfile: UserProfile,
        messageId: MessageId? = null,
        selfIntentId: IntentId,
        activeIntentId: IntentId,
        joinedServiceRegistry: ServiceRegistry = commonServiceRegistry,
        values: Map<String, Any?>,
        otherwiseHandler: suspend (block: suspend () -> Unit) -> Unit
    ): DynamicScope<T> {

        val callContext = CallContext(
            botId = botId,
            chatId = chatId,
            userId = userId,
            profile = userProfile,
            messageId = messageId,
            selfIntentId = selfIntentId,
            activeIntentId = activeIntentId,
            serviceRegistry = joinedServiceRegistry,
            otherwiseHandler = otherwiseHandler
        )


        return scopeFactory.createDynamicScope(
            getIntentById(selfIntentId).scopeClasss as KClass<T>,
            callContext,
            joinedServiceRegistry,
            values
        )
    }

    fun <T : Any> regitsterService(clazz: Class<T>, service: T): BotRunner {
        commonServiceRegistry[clazz] = service
        return this
    }

    fun <T : Any> findService(clazz: Class<T>): T {
        return commonServiceRegistry[clazz]
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BotRunner::class.java)
    }
}

private data class CallArguments(
    val event: InEvent,
    val botState: BotState,
    val serviceRegistry: ServiceRegistry,
    val activeIntentId: IntentId

) {
    val userId: UserId = botState.userId
    val chatId: ChatId = event.chatId
    val messageId: MessageId? = (event as? InMessage)?.messageId
}