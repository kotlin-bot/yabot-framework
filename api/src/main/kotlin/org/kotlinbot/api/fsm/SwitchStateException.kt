package org.kotlinbot.api.fsm

sealed class SwitchStateException : RuntimeException()

class NextStateException : SwitchStateException()
class PrevStateException : SwitchStateException()
class GoToStateException(val stateId: FSMStateId, val delegateEvent: Boolean) : SwitchStateException()