package org.kotlinbot.api

import org.kotlinbot.api.inevents.InEvent
import org.kotlinbot.api.inevents.Text

typealias ReplyCondition<T> = suspend (InEvent?) -> T?

interface ReplyHelper {
    suspend fun <T, E : InEvent> on(predicate: ReplyCondition<T>, action: suspend (event: E, T) -> Unit)

    suspend fun onText(predicate: suspend (String) -> Boolean, action: suspend (textMsg: Text) -> Unit)
    suspend fun <T> onText(predicate: suspend (String) -> T?, action: suspend (textMsg: Text, T) -> Unit)
    suspend fun onText(regexpMatch: Regex, action: suspend (textMsg: Text, matches: MatchResult) -> Unit)
    suspend fun onText(vararg exact: String, action: suspend (textMsg: Text, String) -> Unit)
    suspend fun textOtherwise(action: suspend (textMsg: Text) -> Unit)

    suspend fun otherwise(action: suspend () -> Unit)
}