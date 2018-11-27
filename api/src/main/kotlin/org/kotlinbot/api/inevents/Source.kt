package org.kotlinbot.api.inevents

interface Source {
    val isTelegram: Boolean get() = false
    val isVkontakte: Boolean get() = false
    val isViber: Boolean get() = false
    val isFacebook: Boolean get() = false
    val isInstagram: Boolean get() = false
}