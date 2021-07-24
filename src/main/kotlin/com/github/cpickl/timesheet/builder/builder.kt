@file:JvmName("Builder")

package com.github.cpickl.timesheet.builder

import com.github.cpickl.timesheet.DayOffEntry
import com.github.cpickl.timesheet.EntryDateRange
import com.github.cpickl.timesheet.InputValidationException
import com.github.cpickl.timesheet.TimeEntries
import com.github.cpickl.timesheet.TimeEntry
import com.github.cpickl.timesheet.TimeRange
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
    @Deprecated(message = "year(1) { month(2) { day(3) } }")
    fun day(date: String, code: DayDsl.() -> Unit)

    @Deprecated(message = "year(1) { month(2) { dayOff(3) } }")
    fun dayOff(date: String): DayOffDsl

    fun year(year: Int, code: YearDsl.() -> Unit)

    infix fun DayOffDsl.becauseOf(reason: DayOffReasonDso)
}

interface YearDsl {
    fun month(month: Month, code: YearMonthDsl.() -> Unit)
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
    infix fun tag(tag: BuilderTag)
    operator fun minus(tag: BuilderTag)
}

private class DslImplementation :
    TimeSheetInitDsl, TimeSheetDsl,
    DayDsl, DayOffDsl, PostAboutDsl,
    YearDsl, YearMonthDsl {

    override var freeDays = mutableSetOf<WorkDay>()
    private val entries = mutableListOf<BuilderEntry>()
    private lateinit var currentDay: LocalDate
    private lateinit var currentEntry: BuilderEntry

    // MAIN TIMESHEET DSL
    // ================================================================================================

    private fun _day(newDate: LocalDate, code: DayDsl.() -> Unit) {
        currentDay = newDate
        if (entries.any { it.day == currentDay }) {
            throw BuilderException("Duplicate date entries: ${newDate.toParsableDate()}")
        }

        code()
    }

    private fun _dayOff(newDate: LocalDate): DayOffDsl {
        currentDay = newDate
        currentEntry = BuilderDayOffEntry(currentDay)
        entries += currentEntry
        return this
    }

    override fun day(date: String, code: DayDsl.() -> Unit) {
        _day(date.parseDate(), code)
    }

    override fun dayOff(date: String): DayOffDsl =
        _dayOff(date.parseDate())

    // DAY DSL
    // ================================================================================================

    override operator fun String.minus(description: String) = about(description)

    override infix fun String.about(description: String): PostAboutDsl {
        val timeRangeSpec = TimeRangeSpec.parse(this)
        // overlap validation is done afterwards (as time ranges are built dynamically)
        currentEntry = BuilderWorkDayEntry(currentDay, timeRangeSpec, description)
        entries += currentEntry
        return this@DslImplementation
    }

    // DAY POST ABOUT DSL
    // ================================================================================================

    override fun tag(tag: BuilderTag) {
        val entry = currentEntry as BuilderWorkDayEntry
        entry.tag = tag
    }

    override operator fun minus(tag: BuilderTag) = tag(tag)

    // DAY OFF DSL
    // ================================================================================================

    override fun DayOffDsl.becauseOf(reason: DayOffReasonDso) {
        val entry = currentEntry as BuilderDayOffEntry
        entry.reason = reason
    }

    // BUILD
    // ================================================================================================

    fun build(): TimeSheet {
        val realEntries = try {
            TimeEntries.newValidatedOrThrow(entries.mapIndexed { i, entry ->
                entry.toRealEntry(neighbours = entries.getOrNull(i - 1) to entries.getOrNull(i + 1))
            })
        } catch (e: InputValidationException) {
            throw BuilderException("Invalid timesheet defined: ${e.message}", e)
        }
        return TimeSheet(
            freeDays = freeDays,
            entries = realEntries
        )
    }


    private fun transformTimeRange(timeRangeSpec: TimeRangeSpec, neighbours: Pair<BuilderEntry?, BuilderEntry?>, day: LocalDate): TimeRange =
        when (timeRangeSpec) {
            is TimeRangeSpec.ClosedRangeSpec -> timeRangeSpec.toTimeRange()
            is TimeRangeSpec.OpenStartRangeSpec -> transformOpenAndEndRange(true, timeRangeSpec, neighbours, day)
            is TimeRangeSpec.OpenEndRangeSpec -> transformOpenAndEndRange(false, timeRangeSpec, neighbours, day)
        }

    private fun transformOpenAndEndRange(isStartOpen: Boolean, timeRangeSpec: TimeRangeSpec, neighbours: Pair<BuilderEntry?, BuilderEntry?>, day: LocalDate): TimeRange {
        val label = if (isStartOpen) "start" else "end"
        val labelInversed = if (isStartOpen) "end" else "start"
        val labelNeighbour = if (isStartOpen) "previous" else "following"
        val labelPrefix = "On ${day.toParsableDate()} an invalid open-$label-entry was created '${timeRangeSpec.toParseableString()}': "
        val neighbour = if (isStartOpen) neighbours.first else neighbours.second ?: {
            throw BuilderException("$labelPrefix $labelNeighbour neighbour expected to EXIST!")
        }
        if (neighbour !is BuilderWorkDayEntry) {
            throw BuilderException("$labelPrefix $labelNeighbour neighbour expected to be a WORK day!")
        }
        val requireType = if (isStartOpen) HasEndTime::class else HasStartTime::class
        if (!requireType.isInstance(neighbour.timeRangeSpec)) {
            throw BuilderException("$labelPrefix $labelNeighbour neighbour expected $labelInversed TIME to be defined!")
        }
        return if (isStartOpen) {
            (timeRangeSpec as TimeRangeSpec.OpenStartRangeSpec).toTimeRange(start = (neighbour.timeRangeSpec as HasEndTime).end)
        } else {
            (timeRangeSpec as TimeRangeSpec.OpenEndRangeSpec).toTimeRange(end = (neighbour.timeRangeSpec as HasStartTime).start)
        }
    }

    private fun BuilderEntry.toRealEntry(neighbours: Pair<BuilderEntry?, BuilderEntry?>): TimeEntry = when (this) {
        is BuilderWorkDayEntry -> WorkDayEntry(
            dateRange = EntryDateRange(
                day = day,
                timeRange = transformTimeRange(timeRangeSpec, neighbours, day)
            ),
            about = about,
            tag = tag.realTag,
        )
        is BuilderDayOffEntry -> DayOffEntry(
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

    override fun month(month: Month, code: YearMonthDsl.() -> Unit) {
        currentMonth = month
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
