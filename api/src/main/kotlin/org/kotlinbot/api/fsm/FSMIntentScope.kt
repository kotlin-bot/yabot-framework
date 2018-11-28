package org.kotlinbot.api.fsm

import org.kotlinbot.api.BotScope

interface FSMIntentScope : BotScope {
    var activeStateId: FSMStateId
    var asked: Boolean
}