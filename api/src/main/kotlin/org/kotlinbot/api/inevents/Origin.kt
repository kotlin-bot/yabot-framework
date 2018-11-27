package org.kotlinbot.api.inevents


enum class Origin : Source {
    TG, VK, VIBER, FB, IG, TEXT;

    override val isTelegram: Boolean get() = this == TG
    override val isVkontakte: Boolean get() = this == VK
    override val isViber: Boolean get() = this == VIBER
    override val isFacebook: Boolean get() = this == FB
    override val isInstagram: Boolean get() = this == IG
}