package org.kotlinbot.api


import org.kotlinbot.api.inevents.Callback
import org.kotlinbot.api.inevents.InEvent

typealias BotId = String

val ONLY_BOT: BotId = "onlyBot"
typealias IntentId = String

typealias IntentHandler<SCOPE, TYPE> = suspend SCOPE.(event: TYPE) -> Unit

typealias EventHandler<T> = IntentHandler<T, InEvent>
typealias CallbackHandler<T> = IntentHandler<T, Callback>

typealias BindedEventHandler = suspend (event: InEvent) -> Unit


fun <SCOPE : BotScope, TYPE : InEvent> IntentHandler<SCOPE, TYPE>.after(beforeBlock: IntentHandler<SCOPE, TYPE>)
        : IntentHandler<SCOPE, TYPE> {
    return { event -> beforeBlock(event);this@after.invoke(this, event) }
}
