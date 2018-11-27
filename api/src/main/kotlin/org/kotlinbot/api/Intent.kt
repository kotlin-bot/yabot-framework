package org.kotlinbot.api

import org.kotlinbot.api.IntentActivator.Companion.of
import org.kotlinbot.api.inevents.InEvent

interface Intent<SCOPE : BotScope> {
    val onElect: IntentActivator<SCOPE>? get() = null

    val onStart: EventHandler<SCOPE>? get() = null
    val onEvent: EventHandler<SCOPE>? get() = null
    //вызывается когда в процессе выборов побеждает другой интент и его вызов сопровождается прерываением запущенных
    val onInterrupt: EventHandler<SCOPE>? get() = null
    //вызывается когда в процессе работы интент вызвал другой интент (или был прерван другим интентом) и тот завершил работу
    val onReturn: EventHandler<SCOPE>? get() = null

    val onCallback: CallbackHandler<SCOPE>? get() = null

    companion object {

        fun <SCOPE : BotScope> fromHandler(
            onEvent: EventHandler<SCOPE>? = null,
            onStart: EventHandler<SCOPE>? = onEvent,
            onInterrupt: EventHandler<SCOPE>? = null,
            onReturn: EventHandler<SCOPE>? = null,
            onCallback: CallbackHandler<SCOPE>? = null
        ): Intent<SCOPE> =
            IntentImpl(null, onStart, onEvent, onInterrupt, onReturn, onCallback)

        fun <SCOPE : BotScope> fromHandler(
            //вместо этой логики хорошо бы иметь возможность переключить на интент, отработать и вернуться
            //без возможности делать здесь любые действия
            //но не получается так гибко управлять ресивером
            onElect: IntentActivator<SCOPE>? = null,
            onEvent: EventHandler<SCOPE>? = null,
            onStart: EventHandler<SCOPE>? = onEvent,
            onInterrupt: EventHandler<SCOPE>? = null,
            onReturn: EventHandler<SCOPE>? = null,
            onCallback: CallbackHandler<SCOPE>? = null
        ): Intent<SCOPE> =
            IntentImpl(onElect, onStart, onEvent, onInterrupt, onReturn, onCallback)

        fun <SCOPE : BotScope> fromHandler(
            onElect: (suspend SCOPE.(event: InEvent) -> EventHandler<SCOPE>?)? = null,
            onEvent: EventHandler<SCOPE>? = null,
            onStart: EventHandler<SCOPE>? = onEvent,
            onInterrupt: EventHandler<SCOPE>? = null,
            onReturn: EventHandler<SCOPE>? = null,
            onCallback: CallbackHandler<SCOPE>? = null
        ): Intent<SCOPE> =
            IntentImpl(of(onElect), onStart, onEvent, onInterrupt, onReturn, onCallback)
    }
}

private data class IntentImpl<SCOPE : BotScope>(
    override val onElect: IntentActivator<SCOPE>?,
    override val onStart: EventHandler<SCOPE>?,
    override val onEvent: EventHandler<SCOPE>?,
    override val onInterrupt: EventHandler<SCOPE>?,
    override val onReturn: EventHandler<SCOPE>?,
    override val onCallback: CallbackHandler<SCOPE>?
) : Intent<SCOPE>
