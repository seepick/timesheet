package com.github.seepick.timesheet.dsl

import com.github.seepick.timesheet.contract.ContractDsl
import com.github.seepick.timesheet.date.Day
import com.github.seepick.timesheet.contract.WorkContract
import com.github.seepick.timesheet.timesheet.DayOffEntry
import com.github.seepick.timesheet.contract.DefinedWorkContract
import com.github.seepick.timesheet.timesheet.EntryDateRange
import com.github.seepick.timesheet.timesheet.InvalidTimeEntryException
import com.github.seepick.timesheet.timesheet.OffReason
import com.github.seepick.timesheet.tags.Tag
import com.github.seepick.timesheet.timesheet.TimeEntries
import com.github.seepick.timesheet.timesheet.TimeEntry
import com.github.seepick.timesheet.date.TimeRange
import com.github.seepick.timesheet.timesheet.TimeSheet
import com.github.seepick.timesheet.date.WorkDay
import com.github.seepick.timesheet.timesheet.WorkDayEntry
import com.github.seepick.timesheet.contract.transformContracts
import com.github.seepick.timesheet.date.ClosedRangeSpec
import com.github.seepick.timesheet.date.HasEndTime
import com.github.seepick.timesheet.date.HasStartTime
import com.github.seepick.timesheet.date.OpenEndRangeSpec
import com.github.seepick.timesheet.date.OpenStartRangeSpec
import com.github.seepick.timesheet.date.TimeRangeSpec
import com.github.seepick.timesheet.date.toParsableDate
import com.github.seepick.timesheet.off.BuilderDayOffEntry
import com.github.seepick.timesheet.off.BuilderDaysOffEntry
import com.github.seepick.timesheet.off.DayOffDsl
import com.github.seepick.timesheet.off.OffReasons
import com.github.seepick.timesheet.off.ReasonableOffEntry
import com.github.seepick.timesheet.tags.TagDsl
import com.github.seepick.timesheet.tags.Tags
import java.time.LocalDate
import java.time.Month
import kotlin.IllegalStateException

class DslImplementation<TAGS : Tags, OFFS : OffReasons>(
    private val context: TimeSheetContext<TAGS, OFFS>
) :
    TimeSheetDsl,
    ContractDsl,
    WorkDayDsl, DayOffDsl, TagDsl,
    YearDsl, MonthDsl {

    private val entries = mutableListOf<BuilderEntry>()
    private lateinit var currentDay: LocalDate
    private lateinit var currentEntry: BuilderEntry
    private var currentYear = -1 // Int not possible to do lateinit :-/
    private lateinit var currentMonth: Month
    private val contracts = mutableListOf<DefinedWorkContract>()

    // contract DSL
    // ================================================================================================

    override var hoursPerWeek = WorkContract.default.hoursPerWeek

    override var dayOff: WorkDay
        get() = if (daysOff.size == 1) daysOff.first() else throw IllegalStateException("Expected to be 1 dayOff but there were: $daysOff")
        set(value) {
            daysOff = setOf(value)
        }

    override var daysOff: Set<WorkDay> = WorkContract.default.daysOff

    override fun contract(code: ContractDsl.() -> Unit) {
        code()
        contracts += DefinedWorkContract(
            contract = WorkContract(daysOff = daysOff, hoursPerWeek = hoursPerWeek),
            definedAt = currentDay
        )
    }

    // MAIN TIMESHEET DSL
    // ================================================================================================

    override fun year(year: Int, code: YearDsl.() -> Unit) {
        currentYear = year
        code()
    }

    override fun month(month: Month, code: MonthDsl.() -> Unit) {
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

    override fun day(dayLabel: Day, day: Int, code: WorkDayDsl.() -> Unit) {
        verifyDayLabel(dayLabel, day)
        day(day, code)
    }

    private fun dateByCurrentSetYearAndMonth(day: Int) =
        LocalDate.of(currentYear, currentMonth, day)

    private fun verifyDayLabel(dayLabel: Day, day: Int) {
        val currentDate = LocalDate.of(currentYear, currentMonth, day)
        if (currentDate.dayOfWeek != dayLabel.javaDay) {
            throw IllegalArgumentException("Current date [$currentDate] with day [${currentDate.dayOfWeek}] mismatches expected [$dayLabel]!")
        }
    }

    // DAY OFF DSL
    // ================================================================================================

    override fun dayOff(day: Int): DayOffDsl =
        _dayOff(dateByCurrentSetYearAndMonth(day))

    override fun dayOff(dayLabel: WorkDay, day: Int): DayOffDsl {
        verifyDayLabel(dayLabel.day, day)
        return dayOff(day)
    }

    override fun daysOff(days: IntRange): DayOffDsl {
        currentEntry = BuilderDaysOffEntry(currentYear, currentMonth, days)
        entries += currentEntry
        return this
    }

    override fun DayOffDsl.becauseOf(reason: OffReason) {
        val entry = currentEntry as? ReasonableOffEntry
            ?: throw IllegalStateException("Expected entry to be reasonable, but was: $currentEntry")
        if (!context.offs.contains(reason)) {
            // TODO test whether contains off reason
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

    override infix fun String.about(description: String): TagDsl {
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
                // TODO test me; if configure no tags, and this tag requested doesnt exist; throw!
            }
            entry.tags += tag
        }
    }

    override operator fun minus(tag: Tag) = tag(tag)
    override fun minus(tags: List<Tag>) = addTags(tags)


    // BUILD
    // ================================================================================================

    fun build(): TimeSheet {
        val realEntries = try {
            TimeEntries.newValidatedOrThrow(entries.mapIndexed { i, entry ->
                entry.toRealEntry(neighbours = entries.getOrNull(i - 1) to entries.getOrNull(i + 1))
            }.flatten())
        } catch (e: InvalidTimeEntryException) {
            throw BuilderException("Invalid timesheet defined: ${e.message}", e)
        }
        if(contracts.isEmpty()) {
            contracts += DefinedWorkContract(WorkContract.default, realEntries.firstDate)
        }

        return TimeSheet(
            entries = realEntries,
            contracts = transformContracts(contracts, realEntries),
        )
    }

    // ENTRY TRANSFORMATION
    // -------------------------------------------------------

    private fun BuilderEntry.toRealEntry(neighbours: Pair<BuilderEntry?, BuilderEntry?>): List<TimeEntry> =
        when (this) {
            is BuilderWorkDayEntry -> listOf(
                WorkDayEntry(
                    dateRange = EntryDateRange(
                        day = day,
                        timeRange = transformTimeRange(timeRangeSpec, neighbours, day)
                    ),
                    about = about,
                    tags = tags
                )
            )

            is BuilderDayOffEntry -> listOf(
                DayOffEntry(
                    day = day,
                    reason = reason ?: throw BuilderException("no day off reason was given for: $this")
                )
            )

            is BuilderDaysOffEntry -> this.dates.map {
                DayOffEntry(
                    day = it,
                    reason = reason ?: throw BuilderException("no day off reason was given for: $this")
                )
            }
            else -> throw UnsupportedOperationException("Unrecognized BuilderEntry type: ${this.javaClass.name}")
        }

    private fun transformTimeRange(
        timeRangeSpec: TimeRangeSpec,
        neighbours: Pair<BuilderEntry?, BuilderEntry?>,
        day: LocalDate
    ): TimeRange =
        when (timeRangeSpec) {
            is ClosedRangeSpec -> timeRangeSpec.toTimeRange()
            is OpenStartRangeSpec -> transformOpenAndEndRange(true, timeRangeSpec, neighbours, day)
            is OpenEndRangeSpec -> transformOpenAndEndRange(false, timeRangeSpec, neighbours, day)
        }

    private fun transformOpenAndEndRange(
        isStartOpen: Boolean,
        timeRangeSpec: TimeRangeSpec,
        neighbours: Pair<BuilderEntry?, BuilderEntry?>,
        day: LocalDate
    ): TimeRange {
        val label = if (isStartOpen) "start" else "end"
        val labelInversed = if (isStartOpen) "end" else "start"
        val labelNeighbour = if (isStartOpen) "previous" else "following"
        val labelPrefix =
            "On ${day.toParsableDate()} an invalid open-$label-entry was created '${timeRangeSpec.toParseableString()}': "
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
            (timeRangeSpec as OpenStartRangeSpec).toTimeRange(start = (neighbour.timeRangeSpec as HasEndTime).end)
        } else {
            (timeRangeSpec as OpenEndRangeSpec).toTimeRange(end = (neighbour.timeRangeSpec as HasStartTime).start)
        }
    }
}
