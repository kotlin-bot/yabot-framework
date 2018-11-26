package org.kotlinbot.api

import com.vdurmont.emoji.EmojiParser
import org.kotlinbot.api.inevents.InEvent
import org.kotlinbot.api.inevents.Text

data class ReplyHelperImpl(val callScope: BotScope, val inEvent: InEvent?) :
    ReplyHelper {
    private var matched = false
    private val msg = inEvent


    override suspend fun onText(predicate: suspend (String) -> Boolean, action: suspend () -> Unit) {
        if (!matched && msg is Text && (predicate(msg.message) || predicate(EmojiParser.removeAllEmojis(msg.message).trim())))
            execute(action)
    }


    override suspend fun <T> onText(predicate: suspend (String) -> T?, action: suspend (T) -> Unit) {
        if (!matched && msg is Text)
            (predicate(msg.message)
                ?: predicate(EmojiParser.removeAllEmojis(msg.message).trim()))?.let { execute(action, it) }
    }


    override suspend fun onText(vararg exact: String, action: suspend (String) -> Unit) = onText({ msgText ->
        val onlyText = msgText.replace("[.,:;!?()<>\\[\\]{}]".toRegex(), "").trim()
        exact.find { it.equals(onlyText, true) }
    }, action)

    override suspend fun onText(regexpMatch: Regex, action: suspend (MatchResult) -> Unit) =
        onText({ regexpMatch.find(it) }, action)


    override suspend fun textOtherwise(action: suspend () -> Unit) {
        if (!matched && msg is Text) {
            require(msg is Text)
            execute(action)
        }
    }

    override suspend fun otherwise(action: suspend () -> Unit) {
        if (!matched) {
            callScope.otherwise(action)
            matched = true
        }
    }

    private suspend fun execute(action: suspend () -> Unit) {
        action()
        matched = true
    }

    private suspend fun <T> execute(action: suspend (T) -> Unit, arg: T) {
        action(arg)
        matched = true
    }

    override suspend fun <T> on(predicate: ReplyCondition<T>, action: suspend (T) -> Unit) {
        if (!matched)
            predicate(msg)?.let { execute(action, it) }
    }
}

fun replyRouter(callScope: BotScope, event: InEvent): ReplyHelper {
    return ReplyHelperImpl(callScope, event)
}