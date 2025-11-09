@file:JvmName("Dsl")

package com.github.seepick.timesheet.dsl

import com.github.seepick.timesheet.contract.ContractDsl
import com.github.seepick.timesheet.date.Day
import com.github.seepick.timesheet.timesheet.OffReason
import com.github.seepick.timesheet.tags.Tag
import com.github.seepick.timesheet.timesheet.TimeSheet
import com.github.seepick.timesheet.date.WorkDay
import com.github.seepick.timesheet.off.DayOffDsl
import com.github.seepick.timesheet.off.OffReasons
import com.github.seepick.timesheet.tags.TagDsl
import com.github.seepick.timesheet.tags.Tags
import java.time.Month

fun <TAGS : Tags, OFF : OffReasons> timesheet(
    tags: TAGS,
    offs: OFF,
    entryCode: TimeSheetDsl.() -> Unit
): TimeSheet {
    val context = TimeSheetContext(tags, offs)
    val dsl = DslImplementation(context)
    dsl.entryCode()
    return dsl.build()
}

class TimeSheetContext<TAGS : Tags, OFF : OffReasons>(
    val tags: TAGS,
    val offs: OFF,
)

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class TimesheetAppDsl

@TimesheetAppDsl
interface TimeSheetDsl {
    fun year(year: Int, code: YearDsl.() -> Unit)

    infix fun DayOffDsl.becauseOf(reason: OffReason)
}

interface YearDsl {
    fun month(month: Month, code: MonthDsl.() -> Unit)
}

@TimesheetAppDsl
interface MonthDsl {
    fun day(day: Int, code: WorkDayDsl.() -> Unit)
    fun day(dayLabel: Day, day: Int, code: WorkDayDsl.() -> Unit)
    fun dayOff(day: Int): DayOffDsl
    fun dayOff(dayLabel: WorkDay, day: Int): DayOffDsl
    fun daysOff(days: IntRange): DayOffDsl

    // necessary duplicate
    infix fun DayOffDsl.becauseOf(reason: OffReason)
}

@TimesheetAppDsl
interface WorkDayDsl {
    fun contract(code: ContractDsl.() -> Unit)
    infix fun String.about(description: String): TagDsl
    operator fun String.minus(description: String): TagDsl
}
