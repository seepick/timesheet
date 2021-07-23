@file:JvmName("Builder")

package com.github.cpickl.timesheet.builder

import com.github.cpickl.timesheet.DayOffEntry
import com.github.cpickl.timesheet.EntryDateRange
import com.github.cpickl.timesheet.TimeEntries
import com.github.cpickl.timesheet.TimeEntry
import com.github.cpickl.timesheet.TimeSheet
import com.github.cpickl.timesheet.WorkDay
import com.github.cpickl.timesheet.WorkDayEntry
import java.time.LocalDate
import java.time.Month

fun timesheet(initCode: TimeSheetInitDsl.() -> Unit = {}, entryCode: TimeSheetDsl.() -> Unit): TimeSheet {
    val dsl = DslImplementation()
    dsl.initCode()
    dsl.entryCode()
    return dsl.build()
}

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class TimesheetAppDsl

interface TimeSheetInitDsl {
    var freeDays: MutableSet<WorkDay>
}

@TimesheetAppDsl
interface TimeSheetDsl {
    fun day(date: String, code: DayDsl.() -> Unit)
    fun dayOff(date: String): DayOffDsl

    fun year(year: Int, code: YearDsl.() -> Unit)

    infix fun DayOffDsl.becauseOf(reason: DayOffReasonDso)
}

interface YearDsl {
    fun month(month: Int, code: YearMonthDsl.() -> Unit)
}

@TimesheetAppDsl
interface YearMonthDsl {
    fun day(day: Int, code: DayDsl.() -> Unit)
    fun dayOff(day: Int): DayOffDsl

    // necessary duplicate
    infix fun DayOffDsl.becauseOf(reason: DayOffReasonDso)
}


@TimesheetAppDsl
interface DayDsl {
    infix fun String.about(description: String): PostAboutDsl
    operator fun String.minus(description: String): PostAboutDsl

}

interface DayOffDsl {
}

interface PostAboutDsl {
    infix fun tag(tag: TagDso)
    operator fun minus(tag: TagDso)
}

private class DslImplementation :
    TimeSheetInitDsl, TimeSheetDsl,
    DayDsl, DayOffDsl, PostAboutDsl,
    YearDsl, YearMonthDsl {

    override var freeDays = mutableSetOf<WorkDay>()
    private val entries = mutableListOf<IntermediateEntryDso>()
    private lateinit var currentDay: LocalDate
    private lateinit var currentEntry: IntermediateEntryDso

    // MAIN TIMESHEET DSL
    // ================================================================================================

    private fun _day(newDate: LocalDate, code: DayDsl.() -> Unit) {
        currentDay = newDate
        if (entries.any { it.day == currentDay }) {
            throw BuilderException("Duplicate date entries: ${newDate.toParsableDate()}")
        }

        code()
    }

    override fun day(date: String, code: DayDsl.() -> Unit) {
        _day(date.parseDate(), code)
    }

    private fun _dayOff(newDate: LocalDate): DayOffDsl {
        currentDay = newDate
        currentEntry = DayOffEntryDso(currentDay)
        entries += currentEntry
        return this
    }

    override fun dayOff(date: String): DayOffDsl =
        _dayOff(date.parseDate())

    // DAY DSL
    // ================================================================================================

    override operator fun String.minus(description: String) = about(description)

    override infix fun String.about(description: String): PostAboutDsl {
        val timeRange = this.parseTimeRange()
        currentEntry = IntermediateWorkDayEntryDso(currentDay, timeRange, description)
        entries.filter { it.day == currentDay }
            .filterIsInstance<IntermediateWorkDayEntryDso>()
            .firstOrNull { it.timeRange.overlaps(timeRange) }
            ?.let {
                throw BuilderException("Overlap in time for the day ${currentDay.toParsableDate()}: ${timeRange.toParseableString()}!")
            }
        entries += currentEntry
        return this@DslImplementation
    }

    // DAY POST ABOUT DSL
    // ================================================================================================

    override fun tag(tag: TagDso) {
        val entry = currentEntry as IntermediateWorkDayEntryDso
        entry.tag = tag
    }

    override operator fun minus(tag: TagDso) = tag(tag)

    // DAY OFF DSL
    // ================================================================================================

    override fun DayOffDsl.becauseOf(reason: DayOffReasonDso) {
        val entry = currentEntry as DayOffEntryDso
        entry.reason = reason
    }

    // BUILD
    // ================================================================================================

    fun build(): TimeSheet {
        validate()
        return TimeSheet(
            freeDays = freeDays,
            entries = TimeEntries(entries.map { it.toRealEntry() })
        )
    }

    private fun validate() {
        if (entries.isEmpty()) {
            throw BuilderException("Why you wanna try to build a timesheet without any entries? That has no sense, non-sense!")
        }
        if (entries.first() !is IntermediateWorkDayEntryDso) {
            throw BuilderException("First entry must be a work day (due to... reasons; you wouldn't understand!!!11elf)")
        }
    }

    private fun IntermediateEntryDso.toRealEntry(): TimeEntry = when (this) {
        is IntermediateWorkDayEntryDso -> WorkDayEntry(
            dateRange = EntryDateRange(
                day = day,
                range = timeRange
            ),
            about = about,
            tag = tag.realTag,
        )
        is DayOffEntryDso -> DayOffEntry(
            day = day,
            tag = reason?.realTag
                ?: throw BuilderException("no day off reason was given for: $this")
        )
    }

    // YEAR DSL
    // ================================================================================================

    private var currentYear = -1
    private lateinit var currentMonth: Month

    override fun year(year: Int, code: YearDsl.() -> Unit) {
        currentYear = year
        code()
    }

    override fun month(month: Int, code: YearMonthDsl.() -> Unit) {
        currentMonth = Month.of(month)
        code()
    }

    override fun day(day: Int, code: DayDsl.() -> Unit) {
        _day(dateByCurrentSetYearAndMonth(day), code)
    }

    override fun dayOff(day: Int): DayOffDsl =
        _dayOff(dateByCurrentSetYearAndMonth(day))

    private fun dateByCurrentSetYearAndMonth(day: Int) =
        LocalDate.of(currentYear, currentMonth, day)

}
