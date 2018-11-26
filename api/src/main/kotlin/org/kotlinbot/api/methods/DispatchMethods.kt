package org.kotlinbot.api.methods

import org.kotlinbot.api.IntentId

interface DispatchMethods {
    val selfIntentId: IntentId

    @Throws(DispatchException::class)
    fun start(intentId: IntentId, interrupt: Boolean = false): Unit =
        throw DispatchException(startIntentId = intentId, interrupt = interrupt)

    @Throws(DispatchException::class)
    fun finish(): Unit = throw DispatchException(finishIntentId = selfIntentId)

    @Throws(DispatchException::class)
    fun finishThisAndStart(intentId: IntentId): Unit =
        throw DispatchException(finishIntentId = selfIntentId, startIntentId = intentId)
}

data class DispatchException(
    val finishIntentId: IntentId? = null,
    val startIntentId: IntentId? = null,
    val interrupt: Boolean = false
) :
    RuntimeException()