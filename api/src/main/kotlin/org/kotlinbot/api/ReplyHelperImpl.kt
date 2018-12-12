package org.kotlinbot.api

import com.vdurmont.emoji.EmojiParser
import org.kotlinbot.api.inevents.InEvent
import org.kotlinbot.api.inevents.Text

data class ReplyHelperImpl(val callScope: BotScope, val inEvent: InEvent?) :
    ReplyHelper {
    private var matched = false
    private val msg = inEvent


    override suspend fun onText(predicate: suspend (String) -> Boolean, action: suspend (textMsg: Text) -> Unit) {
        if (!matched && msg is Text && (
                    predicate(msg.message)
                            || predicate(EmojiParser.removeAllEmojis(msg.message).trim())
                    )
        )
            execute(action)
    }


    override suspend fun <T> onText(predicate: suspend (String) -> T?, action: suspend (textMsg: Text, T) -> Unit) {
        if (!matched && msg is Text)
            (predicate(msg.message)
                ?: predicate(EmojiParser.removeAllEmojis(msg.message).trim()))?.let { execute(action, it) }
    }


    override suspend fun onText(
        vararg exact: String,
        action: suspend (textMsg: Text, String) -> Unit
    ) = onText({ msgText ->
        val onlyText = msgText.replace("[.,:;!?()<>\\[\\]{}]".toRegex(), "").trim()
        exact.find { it.equals(onlyText, true) }
    }, action)

    override suspend fun onText(regexpMatch: Regex, action: suspend (textMsg: Text, matches: MatchResult) -> Unit) =
        onText({ regexpMatch.find(it) }, action)


    override suspend fun textOtherwise(action: suspend (textMsg: Text) -> Unit) {
        if (!matched && msg is Text) {
            callScope.otherwise { action(msg) }
            matched = true
        }
    }

    override suspend fun otherwise(action: suspend () -> Unit) {
        if (!matched) {
            callScope.otherwise(action)
            matched = true
        }
    }

    private suspend fun <E : InEvent> execute(action: suspend (E) -> Unit) {
        action(msg as E)
        matched = true
    }

    private suspend fun <T, E : InEvent> execute(action: suspend (E, T) -> Unit, arg: T) {
        action(msg as E, arg)
        matched = true
    }

    override suspend fun <T, E : InEvent> on(predicate: ReplyCondition<T>, action: suspend (event: E, T) -> Unit) {
        if (!matched)
            predicate(msg)?.let { execute(action, it) }
    }
}

fun replyRouter(callScope: BotScope, event: InEvent?): ReplyHelper {
    return ReplyHelperImpl(callScope, event)
}