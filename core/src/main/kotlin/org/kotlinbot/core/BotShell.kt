package org.kotlinbot.core


import org.kotlinbot.api.IntentEventHandler
import org.kotlinbot.api.Scope
import org.kotlinbot.core.platform.scope.classProps
import org.slf4j.Logger
import org.slf4j.LoggerFactory

typealias CommonIntentPropsSplit = Pair<Map<String, Any?>, Map<String, Any?>>

//оболочка бота
//собирает вместе все его интенты/org.kotlinbot.scope + ресстр своих сервисов
class BotShell(
    val handlers: List<IntentEventHandler>,
    val serviceRegistry: ServiceRegistry = ServiceRegistry()
) {
    fun <T : Any> registerService(cls: Class<T>, value: T): BotShell {
        serviceRegistry[cls] = value
        return this
    }

    val commonPropNames = commonPropNames(
        handlers.map { it.scopeClasss.java }.toTypedArray(),
        classProps(Scope::class.java),
        Scope::class.java
    )

    fun splitPropsMap(map: Map<String, Any?>): CommonIntentPropsSplit {
        val common: MutableMap<String, Any?> = HashMap()
        val intent: MutableMap<String, Any?> = HashMap()

        map.forEach { name, value ->
            val store =
                if (name in commonPropNames)
                    common
                else
                    intent
            store[name] = value
        }
        return common to intent
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BotRunner::class.java)
    }
}