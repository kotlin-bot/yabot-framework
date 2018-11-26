package org.kotlinbot.core

import org.kotlinbot.core.platform.scope.propName

fun commonPropNames(
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

fun scanParrentInterfaces(
    classItem: Class<*>,
    accumulator: MutableMap<Class<*>, Int>,
    vararg limitUpperClasses: Class<*>
) {
    accumulator[classItem] = accumulator.getOrPut(classItem, { 0 }) + 1
    classItem.interfaces
        .filter { it !in limitUpperClasses }
        .forEach { scanParrentInterfaces(it, accumulator, *limitUpperClasses) }
}