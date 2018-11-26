package org.kotlinbot.scope

import org.kotlinbot.api.BotScope

interface Scope1 : BotScope {
    var stringParam: String
    var booleanParam: Boolean
    var intParam: Int
    var longParam: Long
}

interface Scope2 : BotScope {
    var param2: String
}

interface Scope2_1 : Scope1 {
    var param2_2: Boolean
}