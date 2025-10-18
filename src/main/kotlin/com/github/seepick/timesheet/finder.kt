@file:JvmName("Finder")

package com.github.seepick.timesheet

fun loadAutoSheetFromClasspath(fqn: String): com.github.seepick.timesheet.AutoSheet? {
    val clazz = try {
        Class.forName(fqn)
    } catch (e: ClassNotFoundException) {
        return null
    }
    return clazz.declaredConstructors[0].newInstance() as com.github.seepick.timesheet.AutoSheet
}
