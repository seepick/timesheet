@file:JvmName("Finder")

package com.github.cpickl.timesheet

fun loadAutoSheetFromClasspath(fqn: String): AutoSheet? {
    val clazz = try {
        Class.forName(fqn)
    } catch (e: ClassNotFoundException) {
        return null
    }
    return clazz.declaredConstructors[0].newInstance() as AutoSheet
}
