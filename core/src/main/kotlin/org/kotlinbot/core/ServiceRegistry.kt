package org.kotlinbot.core

import org.kotlinbot.core.platform.scope.ScopeFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.lang.reflect.Proxy


data class ServiceRegistry(val registry: MutableMap<Class<*>, Any> = HashMap(), val parent: ServiceRegistry? = null) {

    operator fun <T> get(clazz: Class<T>): T {
        val instance = getOrNull(clazz)
        if (instance == null) {
            logger.warn("No {} implementation was provided", clazz.simpleName)
        }

        return instance ?: dontCallMeImpl(clazz)
    }

    fun <T> getOrNull(clazz: Class<T>): T? = registry[clazz] as T? ?: parent?.getOrNull(clazz)

    operator fun <T : Any> set(clazz: Class<T>, service: T): ServiceRegistry {
        registry.put(clazz, service)
        return this
    }

    fun <T : Any> registerIfAbsent(cls: Class<T>, service: T): ServiceRegistry {
        registry.putIfAbsent(cls, service)
        return this
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ServiceRegistry::class.java)

        fun <T> dontCallMeImpl(cls: Class<T>): T {
            return Proxy.newProxyInstance(
                ScopeFactory::class.java.classLoader,
                arrayOf(cls)
            ) { proxy: Any, method: Method, args: Array<Any> ->
                error("To call ${cls.simpleName}::${method.name} you must provide impl via service registry")
            } as T
        }
    }
}

