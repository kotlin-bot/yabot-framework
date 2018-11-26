package org.kotlinbot.api.inevents

enum class Origin {
    TG,VK,VIBER,FB,IG,TEXT;

    val isTelegram: Boolean get() = this==TG
    val isVkontakte: Boolean get() = this==VK
    val isViber: Boolean get() = this==VIBER
    val isFacebook: Boolean get() = this==FB
    val isInstagram: Boolean get() = this==IG
}