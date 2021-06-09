package com.github.cpickl.timesheet

interface TimeSheetFinder {
    fun find(): TimeSheet
}

class ClasspathTimesheetFinder(
    private val fqn: String
) : TimeSheetFinder {
    override fun find(): TimeSheet {
        return (Class.forName(fqn).declaredConstructors[0].newInstance() as TimeSheetProvider).provide()
    }
}
