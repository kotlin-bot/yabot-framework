package org.kotlinbot.api.fsm

import org.kotlinbot.api.CallbackHandler
import org.kotlinbot.api.EventHandler
import org.kotlinbot.api.Intent
import org.kotlinbot.api.IntentActivator
import org.kotlinbot.api.inevents.InEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class FSMIntent<SCOPE : FSMIntentScope>(
    val states: Map<FSMStateId, FSMState<SCOPE>>,
    val startStateId: FSMStateId,
    override val onElect: IntentActivator<SCOPE>? = null,
    override val onInterrupt: EventHandler<SCOPE>? = null,
    override val onCallback: CallbackHandler<SCOPE>? = null
) : Intent<SCOPE> {

    constructor(stateList: Collection<out FSMState<SCOPE>>) : this(
        states = stateList.map { it.stateId to it }.toMap(),
        startStateId = stateList.firstOrNull()?.stateId ?: error("empty satates list is not allowed")
    )

    constructor(stateList: Array<out FSMState<SCOPE>>) : this(stateList.toList())

    fun setOnElect(onElect: IntentActivator<SCOPE>) = copy(onElect = onElect)
    fun setOnInterrupt(onInterrupt: EventHandler<SCOPE>) = copy(onInterrupt = onInterrupt)
    fun setOnCallback(onCallback: CallbackHandler<SCOPE>) = copy(onCallback = onCallback)

    override val onStart: EventHandler<SCOPE>?
        get() = { onStartHandler(it) }

    override val onEvent: EventHandler<SCOPE>?
        get() = { onEventHandler(it) }

    override val onReturn: EventHandler<SCOPE>?
        get() = { onReturnHandler(it) }


    suspend fun SCOPE.onStartHandler(event: InEvent) {
        activeStateId = startStateId
        asked = false
        handleStateSwitch(event) {
            with(activeState()) {
                ask(event)
                asked = true
            }
        }

    }

    suspend fun SCOPE.onEventHandler(event: InEvent) {
        handleStateSwitch(event) {
            with(activeState()) {
                handleReply(event)
            }
        }
    }

    suspend fun SCOPE.onReturnHandler(event: InEvent) {

    }

    private suspend fun SCOPE.handleStateSwitch(event: InEvent?, deep: Int = 0, block: suspend SCOPE.() -> Unit) {
        val oldState = activeState()
        try {
            block()
        } catch (e: SwitchStateException) {
            when (e) {
                is GoToStateException -> {
                    if (deep > MAX_CALL_DEPTH)
                        throw IllegalStateException("Recursive state switch in ask-phase in " + this::class.java.simpleName)

                    val delegatedEvent = if (e.delegateEvent) event else null
                    activeStateId = e.stateId
                    val currentState = activeState()
                    if (currentState != oldState) {
                        handleStateSwitch(delegatedEvent, deep + 1) {
                            with(currentState) {
                                asked = false
                                ask(delegatedEvent)
                                asked = true
                            }
                        }
                    }
                }
                else -> error("Not implemented state switch type")

            }
        }
    }

    private fun SCOPE.activeState(): FSMState<SCOPE> {
        assert(states.containsKey(activeStateId)) { "No FSMState for id $activeStateId in intent:# $selfIntentId" }
        return states[activeStateId]!!
    }

    companion object {
        private const val MAX_CALL_DEPTH = 12
        private val logger: Logger = LoggerFactory.getLogger(FSMIntent::class.java)
    }

}