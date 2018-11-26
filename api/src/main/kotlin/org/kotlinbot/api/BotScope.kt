package org.kotlinbot.api

import org.kotlinbot.api.methods.*

interface BotScope : Scope, AttachmentResolver,
    ReplyMethods, ReplyNowMethods,
    DispatchMethods, OtherwiseMethods