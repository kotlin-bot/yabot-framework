package org.kotlinbot.api.fsm

import org.kotlinbot.api.inevents.InEvent

typealias FSMStateId = String

interface FSMState<SCOPE : FSMIntentScope> {
    val stateId: FSMStateId get() = javaClass.simpleName

    suspend fun SCOPE.ask(event: InEvent? = null) {}
    suspend fun SCOPE.handleReply(event: InEvent) {}

    @Throws(SwitchStateException::class)
    fun goToState(stateId: FSMStateId, delegateEvent: Boolean = false): Unit =
        throw GoToStateException(stateId = stateId, delegateEvent = delegateEvent)

    @Throws(SwitchStateException::class)
    fun goToState(state: FSMState<SCOPE>, delegateEvent: Boolean = false): Unit =
        goToState(state.stateId, delegateEvent)
}