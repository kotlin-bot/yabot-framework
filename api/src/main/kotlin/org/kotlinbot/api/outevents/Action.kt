package org.kotlinbot.api.outevents

import org.kotlinbot.api.inevents.UserId

data class Action(override val chatId: UserId) : OutEvent