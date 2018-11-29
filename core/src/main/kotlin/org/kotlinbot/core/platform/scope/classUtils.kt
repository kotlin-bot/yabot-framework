package org.kotlinbot.core.platform.scope

import org.kotlinbot.api.BotScope
import org.kotlinbot.core.platform.scope.MethodType.*
import org.kotlinbot.core.platform.scope.PropType.SERVICE
import org.kotlinbot.core.platform.scope.PropType.VARIABLE
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KType

enum class MethodType(val prefix: String) {
    UNKNOWN(""), SETTER("set"), GETTER("get");
}

enum class PropType {
    VARIABLE, SERVICE
}

typealias ClassPropMap = Map<String, PropDefinition>

internal val SCOPE_OWN_METHODS = BotScope::class.members.map { method ->
    method.name
}.toSet()

fun methodNameToPropName(method: Method): Pair<MethodType, String> {
    val name = method.name
    return when {
        name.startsWith("set") -> SETTER to propName(name)
        name.startsWith("get") -> GETTER to propName(name)
        else -> UNKNOWN to name
    }
}

fun propName(methodName: String) = methodName.substring(3).decapitalize()

internal fun classProps(classItem: Class<*>): Set<String> {
    return classItem.methods
        .map { it.name }
        .filter { it.startsWith("set") }
        .map(::propName)
        .toSet()
}

internal fun commonPropNames(
    clases: Array<Class<*>>,
    exclude: Collection<String>,
    vararg limitUpperClasses: Class<*>
): Set<String> {
    val count = clases.size
    val classUsageStat = HashMap<Class<*>, Int>()
    clases.forEach {
        scanParrentInterfaces(it, classUsageStat, *limitUpperClasses)
    }

    val commonPropNames = classUsageStat
        .filter { it.value >= count }
        .map {
            it.key.methods
                .map { it.name }
                .filter { it.startsWith("set") }
                .map(::propName)
        }.flatten().toSet()


    return commonPropNames.filter { !exclude.contains(it) }.toSet()
}

internal fun scanParrentInterfaces(
    classItem: Class<*>,
    accumulator: MutableMap<Class<*>, Int>,
    vararg limitUpperClasses: Class<*>
) {
    accumulator[classItem] = accumulator.getOrPut(classItem, { 0 }) + 1
    classItem.interfaces
        .filter { it !in limitUpperClasses }
        .forEach { scanParrentInterfaces(it, accumulator, *limitUpperClasses) }
}

internal fun <S> mapClassProperties(scopeClass: Class<S>): MutableMap<String, Pair<PropType, Class<*>>> {
    val props = scopeClass.methods
        .filter { method -> !SCOPE_OWN_METHODS.contains(method.name) }
        .map {
            val (type, propName) = methodNameToPropName(it)
            Triple(propName, type, it.returnType)
        }.groupBy { it.first }

    val result: MutableMap<String, Pair<PropType, Class<*>>> = HashMap()


    props
        .forEach { (name, methods) ->
            val propType = when (methods.size) {
                1 -> SERVICE
                2 -> VARIABLE
                else -> error("To much cnadidates for prop. Bug")
            }
            val (_, _, returnType) = methods
                .first { (_, methodType, _) -> methodType == GETTER }

            result[name] = propType to returnType
        }

    return result
}

data class PropDefinition(
    val propType: PropType,
    val name: String,
    val nullable: Boolean,
    val type: KType
)

internal fun <S : Any> mapClassProperties(scopeClass: KClass<S>): Map<String, PropDefinition> {
    return scopeClass.members
        .filter { prop -> !SCOPE_OWN_METHODS.contains(prop.name) }
        .map { prop ->
            prop.name to PropDefinition(
                propType = if (prop is KMutableProperty) VARIABLE else SERVICE,
                name = prop.name,
                nullable = prop.returnType.isMarkedNullable,
                type = prop.returnType
            )
        }.toMap()
}