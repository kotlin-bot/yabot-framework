package org.kotlinbot.runner

fun ensureClassExists(fqdcn: String, message: String) {
    try {
        Class.forName(fqdcn)
    } catch (e: ClassNotFoundException) {
        error(message)
    }
}