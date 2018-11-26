package org.kotlinbot.api

import org.kotlinbot.api.inevents.InEvent

typealias ReplyCondition<T> = suspend (InEvent?) -> T?

interface ReplyHelper {
    suspend fun <T> on(predicate: ReplyCondition<T>, action: suspend (T) -> Unit)

    suspend fun onText(predicate: suspend (String) -> Boolean, action: suspend () -> Unit)
    suspend fun <T> onText(predicate: suspend (String) -> T?, action: suspend (T) -> Unit)
    suspend fun onText(regexpMatch: Regex, action: suspend (MatchResult) -> Unit)
    suspend fun onText(vararg exact: String, action: suspend (String) -> Unit)

    suspend fun textOtherwise(action: suspend () -> Unit)
    suspend fun otherwise(action: suspend () -> Unit)
}