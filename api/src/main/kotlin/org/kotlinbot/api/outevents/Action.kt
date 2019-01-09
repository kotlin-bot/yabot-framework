package org.kotlinbot.api.outevents

import org.kotlinbot.api.inevents.PersonId

data class Action(override val chatId: PersonId) : OutEvent