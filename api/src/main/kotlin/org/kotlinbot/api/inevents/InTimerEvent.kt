package org.kotlinbot.api.inevents

typealias TimerId = Long

interface InTimerEvent : InEvent {
    val timerId: TimerId
}