package org.kotlinbot.core.platform.scope

import org.kotlinbot.api.BotScope
import org.kotlinbot.core.ServiceRegistry


class ScopeFactory {
    val classInfoCache: MutableMap<String, ClassPropMap> = HashMap()

    fun <T : BotScope> createScope(
        cls: Class<T>,
        callContext: CallContext,
        serviceRegistry: ServiceRegistry,
        values: Map<String, Any?>
    ): T {
        return createDynamicScope(cls, callContext, serviceRegistry, values).asScope()
    }

    fun <T : BotScope> createDynamicScope(
        cls: Class<T>,
        callContext: CallContext,
        serviceRegistry: ServiceRegistry,
        values: Map<String, Any?>
    ): DynamicScope<T> {
        return DynamicScope(
            cls,
            classInfoCache.getOrPut(cls.canonicalName) { mapClassProperties(cls) },
            callContext,
            serviceRegistry,
            HashMap(values)
        )
    }


}

