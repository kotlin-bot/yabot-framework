package org.kotlinbot.api

enum class CallMode {
    //interrupt active intent to root and start another intent
    INTERRUPT,
    //start another intent like it was called
    START,
    //dont start another intent - just do the job and finish
    NO_START
}