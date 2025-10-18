package com.github.seepick.timesheet.builder

import com.github.seepick.timesheet.DayOffEntry
import com.github.seepick.timesheet.EntryDateRange
import com.github.seepick.timesheet.InputValidationException
import com.github.seepick.timesheet.OffReason
import com.github.seepick.timesheet.Tag
import com.github.seepick.timesheet.TimeEntries
import com.github.seepick.timesheet.TimeEntry
import com.github.seepick.timesheet.TimeRange
import com.github.seepick.timesheet.TimeSheet
import com.github.seepick.timesheet.WorkDay
import com.github.seepick.timesheet.WorkDayEntry
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.Month

internal class DslImplementation<TAGS : Tags, OFFS : OffReasons>(
    private val context: TimeSheetContext<TAGS, OFFS>
) :
    TimeSheetInitDsl, TimeSheetDsl,
    WorkDayDsl, DayOffDsl, PostAboutDsl,
    YearDsl, YearMonthDsl {

    override var daysOff = mutableSetOf<WorkDay>()
    private val entries = mutableListOf<BuilderEntry>()
    private lateinit var currentDay: LocalDate
    private lateinit var currentEntry: BuilderEntry
    private var currentYear = -1 // Int not possible to do lateinit :-/
    private lateinit var currentMonth: Month

    // MAIN TIMESHEET DSL
    // ================================================================================================


    override fun year(year: Int, code: YearDsl.() -> Unit) {
        currentYear = year
        code()
    }

    override fun month(month: Month, code: YearMonthDsl.() -> Unit) {
        currentMonth = month
        code()
    }

    override fun day(day: Int, code: WorkDayDsl.() -> Unit) {
        val newDate = dateByCurrentSetYearAndMonth(day)
        if (entries.any { it.matches(newDate) }) {
            throw BuilderException("Duplicate date entries: ${newDate.toParsableDate()}")
        }
        currentDay = newDate
        code()

    }

    private fun dateByCurrentSetYearAndMonth(day: Int) =
        LocalDate.of(currentYear, currentMonth, day)

    // DAY OFF DSL
    // ================================================================================================

    override fun dayOff(day: Int): DayOffDsl =
        _dayOff(dateByCurrentSetYearAndMonth(day))

    override fun daysOff(days: IntRange): DayOffDsl {
        currentEntry = BuilderDaysOffEntry(currentYear, currentMonth, days)
        entries += currentEntry
        return this
    }

    override fun DayOffDsl.becauseOf(reason: OffReason) {
        val entry = currentEntry as? ReasonableOffEntry ?: throw IllegalStateException("Expected entry to be reasonable, but was: $currentEntry")
        if (!context.offs.contains(reason)) {
            // FIXME test whether contains off reason
        }
        entry.reason = reason
    }

    private fun _dayOff(newDate: LocalDate): DayOffDsl {
        currentDay = newDate // TODO needed to set?!
        currentEntry = BuilderDayOffEntry(currentDay)
        entries += currentEntry
        return this
    }

    // DAY DSL
    // ================================================================================================

    override infix fun String.about(description: String): PostAboutDsl {
        val timeRangeSpec = TimeRangeSpec.parse(this)
        // overlap validation is done afterwards (as time ranges are built dynamically)
        currentEntry = BuilderWorkDayEntry(currentDay, timeRangeSpec, description)
        entries += currentEntry
        return this@DslImplementation
    }

    override operator fun String.minus(description: String) = about(description)

    // DAY POST ABOUT DSL
    // -------------------------------------------------------

    override fun tag(tag: Tag) {
        addTags(listOf(tag))
    }

    override fun tags(tag1: Tag, vararg moreTags: Tag) {
        addTags(listOf(tag1, *moreTags))
    }

    private fun addTags(allTags: List<Tag>) {
        val entry = currentEntry as BuilderWorkDayEntry
        allTags.forEach { tag ->
            if (!context.tags.contains(tag)) {
                // FIXME test me; if configure no tags, and this tag requested doesnt exist; throw!
            }
            entry.tags += tag
        }
    }

    override operator fun minus(tag: Tag) = tag(tag)
    override fun minus(tags: List<Tag>)  = addTags(tags)


    // BUILD
    // ================================================================================================

    fun build(): TimeSheet {
        val realEntries = try {
            TimeEntries.newValidatedOrThrow(entries.mapIndexed { i, entry ->
                entry.toRealEntry(neighbours = entries.getOrNull(i - 1) to entries.getOrNull(i + 1))
            }.flatten())
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

    private fun BuilderEntry.toRealEntry(neighbours: Pair<BuilderEntry?, BuilderEntry?>): List<TimeEntry> = when (this) {
        is BuilderWorkDayEntry -> listOf(WorkDayEntry(
            dateRange = EntryDateRange(
                day = day,
                timeRange = transformTimeRange(timeRangeSpec, neighbours, day)
            ),
            about = about,
            tags = tags
        ))
        is BuilderDayOffEntry -> listOf(DayOffEntry(
            day = day,
            reason = reason ?: throw BuilderException("no day off reason was given for: $this")
        ))
        is BuilderDaysOffEntry -> this.dates.map {
            DayOffEntry(
                day = it,
                reason = reason ?: throw BuilderException("no day off reason was given for: $this")
            )
        }
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
