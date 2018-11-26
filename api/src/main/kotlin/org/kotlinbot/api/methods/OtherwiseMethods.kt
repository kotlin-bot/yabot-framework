package org.kotlinbot.api.methods

interface OtherwiseMethods{
    suspend fun otherwise(block: suspend () -> Unit = {})
}