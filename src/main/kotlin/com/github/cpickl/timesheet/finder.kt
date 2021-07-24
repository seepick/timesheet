@file:JvmName("Finder")

package com.github.cpickl.timesheet

interface TimeSheetFinder {
    fun find(): TimeSheet
}

class ClasspathTimesheetFinder(
    private val fqn: String
) : TimeSheetFinder {
    override fun find(): TimeSheet {
        val clazz = try {
            Class.forName(fqn)
        } catch (e: ClassNotFoundException) {
            throw Exception("Could not find class '$fqn' in classpath. Please create own (see README file).")
        }
        return (clazz.declaredConstructors[0].newInstance() as TimeSheetProvider).provide()
    }
}
