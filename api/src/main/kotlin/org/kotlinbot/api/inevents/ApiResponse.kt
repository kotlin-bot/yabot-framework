package org.kotlinbot.api.inevents

interface ApiResponse<RESULT> : InEvent {
    val result: RESULT
}