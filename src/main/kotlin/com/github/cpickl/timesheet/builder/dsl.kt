@file:JvmName("Dsl")

package com.github.cpickl.timesheet.builder

import com.github.cpickl.timesheet.DayOffEntry
import com.github.cpickl.timesheet.EntryDateRange
import com.github.cpickl.timesheet.Tag
import com.github.cpickl.timesheet.InputValidationException
import com.github.cpickl.timesheet.NoTag
import com.github.cpickl.timesheet.OffReason
import com.github.cpickl.timesheet.TimeEntries
import com.github.cpickl.timesheet.TimeEntry
import com.github.cpickl.timesheet.TimeRange
import com.github.cpickl.timesheet.TimeSheet
import com.github.cpickl.timesheet.WorkDay
import com.github.cpickl.timesheet.WorkDayEntry
import java.time.LocalDate
import java.time.Month

interface Tags {
    fun all(): List<Tag>
    fun contains(tag: Tag): Boolean = all().contains(tag)

    companion object
}

abstract class OffReasonsBuilder(

) {

}

interface OffReasons {
    fun all(): List<OffReason>
    fun contains(tag: OffReason): Boolean = all().contains(tag)

    companion object
}

fun <TAGS : Tags, OFF : OffReasons> context(
    tags: TAGS,
    offs: OFF,
    init: TimeSheetInitDsl.() -> Unit = {}
) = TimeContext(tags, offs, init)

class TimeContext<TAGS : Tags, OFF : OffReasons>(
    val tags: TAGS,
    val offs: OFF,
    val init: TimeSheetInitDsl.() -> Unit = {}
)

fun <TAGS : Tags, OFF : OffReasons> timesheet(context: TimeContext<TAGS, OFF>, entryCode: TimeSheetDsl.() -> Unit): TimeSheet {
    val dsl = DslImplementation(context)
    context.init(dsl)
    dsl.entryCode()
    return dsl.build()
}

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class TimesheetAppDsl

interface TimeSheetInitDsl {
    var daysOff: MutableSet<WorkDay>
}

@TimesheetAppDsl
interface TimeSheetDsl {
    @Deprecated(message = "year(1) { month(2) { day(3) } }")
    fun day(date: String, code: WorkDayDsl.() -> Unit)

    @Deprecated(message = "year(1) { month(2) { dayOff(3) } }")
    fun dayOff(date: String): DayOffDsl

    fun year(year: Int, code: YearDsl.() -> Unit)

    infix fun DayOffDsl.becauseOf(reason: OffReason)
}

interface YearDsl {
    fun month(month: Month, code: YearMonthDsl.() -> Unit)
}

@TimesheetAppDsl
interface YearMonthDsl {
    fun day(day: Int, code: WorkDayDsl.() -> Unit)
    fun dayOff(day: Int): DayOffDsl

    // necessary duplicate
    infix fun DayOffDsl.becauseOf(reason: OffReason)
}


@TimesheetAppDsl
interface WorkDayDsl {
    infix fun String.about(description: String): PostAboutDsl
    operator fun String.minus(description: String): PostAboutDsl
}

interface DayOffDsl {
}

interface PostAboutDsl {
    infix fun tag(tag: Tag)
    operator fun minus(tag: Tag)
}

private class DslImplementation<TAGS : Tags, OFFS : OffReasons>(
    private val context: TimeContext<TAGS, OFFS>
) :
    TimeSheetInitDsl, TimeSheetDsl,
    WorkDayDsl, DayOffDsl, PostAboutDsl,
    YearDsl, YearMonthDsl {

    override var daysOff = mutableSetOf<WorkDay>()
    private val entries = mutableListOf<BuilderEntry>()
    private lateinit var currentDay: LocalDate
    private lateinit var currentEntry: BuilderEntry

    // MAIN TIMESHEET DSL
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

    override fun day(day: Int, code: WorkDayDsl.() -> Unit) {
        _day(dateByCurrentSetYearAndMonth(day), code)
    }

    override fun dayOff(day: Int): DayOffDsl =
        _dayOff(dateByCurrentSetYearAndMonth(day))

    private fun dateByCurrentSetYearAndMonth(day: Int) =
        LocalDate.of(currentYear, currentMonth, day)

    // INTERNALS
    // ================================================================================================

    private fun _day(newDate: LocalDate, code: WorkDayDsl.() -> Unit) {
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

    override fun day(date: String, code: WorkDayDsl.() -> Unit) {
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
    // -------------------------------------------------------

    override fun tag(tag: Tag) {
        val entry = currentEntry as BuilderWorkDayEntry
        if (!context.tags.contains(tag)) {
            // FIXME test me; if configure no tags, and this tag requested doesnt exist; throw!
        }
        entry.tag = tag
    }

    override operator fun minus(tag: Tag) = tag(tag)


    // DAY OFF DSL
    // ================================================================================================

    override fun DayOffDsl.becauseOf(reason: OffReason) {
        val entry = currentEntry as BuilderDayOffEntry
        if (!context.offs.contains(reason)) {
            // FIXME test whether contains off reason
        }
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
            freeDays = daysOff,
            entries = realEntries
        )
    }

    // ENTRY TRANSFORMATION
    // -------------------------------------------------------

    private fun BuilderEntry.toRealEntry(neighbours: Pair<BuilderEntry?, BuilderEntry?>): TimeEntry = when (this) {
        is BuilderWorkDayEntry -> WorkDayEntry(
            dateRange = EntryDateRange(
                day = day,
                timeRange = transformTimeRange(timeRangeSpec, neighbours, day)
            ),
            about = about,
            tag = tag ?: NoTag
        )
        is BuilderDayOffEntry -> DayOffEntry(
            day = day,
            reason = reason ?: throw BuilderException("no day off reason was given for: $this")
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


}
