package org.kotlinbot.api

import org.kotlinbot.api.inevents.PersonId
import java.util.*
import kotlin.collections.HashMap

data class BotState(
    val botId: BotId,
    val userId: PersonId,
    val callStack: Deque<String> = ArrayDeque(),
    val activityHistory: Deque<String> = ArrayDeque(),
    val commonState: MutableMap<String, Any?> = HashMap(),
    val intentStates: MutableMap<IntentId, MutableMap<String, Any?>> = HashMap(),
    val personProfile: PersonProfile = PersonProfile(userId)
) {
    fun intentState(intentId: IntentId): Map<String, Any?> {
        return commonState + (intentStates[intentId] ?: emptyMap())
    }

    fun setIntentState(intentId: String, intentScope: Map<String, Any?>) {
        intentStates[intentId] = HashMap(intentScope)
    }

    fun dropIntentState(intentId: IntentId) {
        intentStates.remove(intentId)
    }

    fun startIntent(intentId: IntentId) {
        callStack.addFirst(intentId)
        intentWasActivated(intentId)
    }

    fun finishActiveIntent(interrupt: Boolean = false) {
        val intentId = callStack.pollFirst()
        if (!interrupt)
            dropIntentState(intentId)
    }

    fun activeIntentId(): IntentId {
        return callStack.first()
    }

    fun intentWasActivated(intentId: IntentId) {
        activityHistory.removeAll { it == intentId }
        activityHistory.addFirst(intentId)
        if (activityHistory.size > 20)
            activityHistory.removeLast()
    }

    fun intentToInterruptLeft(): Boolean {
        return callStack.size > 1
    }

}
