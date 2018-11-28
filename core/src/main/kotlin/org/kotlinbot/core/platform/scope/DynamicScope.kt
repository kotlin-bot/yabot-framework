package org.kotlinbot.core.platform.scope

import org.kotlinbot.api.methods.DispatchException
import org.kotlinbot.core.ServiceRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

class DynamicScope<S : Any> constructor(
    private val scopeClass: KClass<S>,
    private val classPropMap: ClassPropMap,
    private val callContext: CallContext,
    private val servicesRegistry: ServiceRegistry,
    val values: MutableMap<String, Any?> = HashMap()
) {


    fun asScope(): S {
        return Proxy.newProxyInstance(
            ScopeFactory::class.java.classLoader,
            arrayOf(scopeClass.java),
            this::proxyCallHandler
        ) as S
    }

    @Throws(DispatchException::class)
    private fun proxyCallHandler(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        if (SCOPE_OWN_METHODS.contains(method!!.name)) {
            //делегируем вызов callContext
            try {
                return method.invoke(callContext, *(args ?: emptyArray()))
            } catch (e: InvocationTargetException) {
                if (e.targetException is DispatchException)
                    throw e.targetException
                else {
                    log.error("Error while calling scopewide intentMethod {}", method.name, e)
                    throw e
                }
            }
        } else if (method.name == "toString") {
            with(callContext) {
                return "Scope ${scopeClass.simpleName} [user=$chatId\nBot+Intent Props:${values}"
            }
        } else {

            val (methodType, propName) = methodNameToPropName(method)
            return when (methodType) {
                MethodType.SETTER -> values[propName] = args?.firstOrNull()
                MethodType.GETTER -> {
                    val (propType, _, nullable, resultClass) = classPropMap[propName]!!
                    if (propType == PropType.VARIABLE)
                        return loadAndTransformProp(propName, nullable, resultClass)
                    else {
                        val service = servicesRegistry.getOrNull(resultClass)
                        if (service == null)
                            log.warn("\nNo service registred for class ${resultClass}::${propName}")


                        return service
                    }
                }
                MethodType.UNKNOWN -> TODO("Unknown how to handle intentMethod ${method.name}")
            }
        }
    }

    private fun loadAndTransformProp(propName: String, nullable: Boolean, resultClass: Class<*>): Any? {
        return values[propName] ?: if (nullable) null else mabyWeCanProvideDefaultValues(resultClass)
    }

    private fun mabyWeCanProvideDefaultValues(resultClass: Class<*>): Any? {
        return when (resultClass) {
            Boolean::class.java -> false
            String::class.java -> ""
            Int::class.java -> 0.toInt()
            Byte::class.java -> 0.toByte()
            Short::class.java -> 0.toShort()
            Long::class.java -> 0.toLong()
            Double::class.java -> 0.toDouble()
            Float::class.java -> 0.toFloat()
            else -> null
        }
    }

    companion object {
        val log: Logger =
            LoggerFactory.getLogger(DynamicScope::class.java)
    }
}