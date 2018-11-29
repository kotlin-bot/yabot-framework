package org.kotlinbot.core.platform.scope

import org.kotlinbot.api.methods.DispatchException
import org.kotlinbot.core.ServiceRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaType

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
                        val service = servicesRegistry.getOrNull(resultClass.javaType as Class<*>)
                        if (service == null)
                            log.warn("\nNo service registred for class ${resultClass}::${propName}")


                        return service
                    }
                }
                MethodType.UNKNOWN -> TODO("Unknown how to handle intentMethod ${method.name}")
            }
        }
    }

    private fun loadAndTransformProp(propName: String, nullable: Boolean, resultClass: KType): Any? {
        return values[propName] ?: if (nullable) null else maybeWeCanProvideDefaultValues(resultClass)
    }

    private fun maybeWeCanProvideDefaultValues(resultType: KType): Any? {
        //resultType.isSubtypeOf(Collection::class.starProjectedType)
        return when {
            resultType.isSubtypeOf(MutableSet::class.starProjectedType) -> HashSet<Any>()
            resultType.isSubtypeOf(Set::class.starProjectedType) -> emptySet<Any>()

            resultType.isSubtypeOf(MutableList::class.starProjectedType) -> ArrayList<Any>()
            resultType.isSubtypeOf(List::class.starProjectedType) -> emptyList<Any>()

            resultType.isSubtypeOf(MutableMap::class.starProjectedType) -> HashMap<Any, Any?>()
            resultType.isSubtypeOf(Map::class.starProjectedType) -> emptyMap<Any, Any?>()

            resultType.javaType == Boolean::class.java -> false
            resultType.javaType == String::class.java -> ""
            resultType.javaType == Int::class.java -> 0.toInt()
            resultType.javaType == Byte::class.java -> 0.toByte()
            resultType.javaType == Short::class.java -> 0.toShort()
            resultType.javaType == Long::class.java -> 0.toLong()
            resultType.javaType == Double::class.java -> 0.toDouble()
            resultType.javaType == Float::class.java -> 0.toFloat()
            else -> null
        }
    }

    companion object {
        val log: Logger =
            LoggerFactory.getLogger(DynamicScope::class.java)
    }
}