package com.github.seepick.timesheet.dsl

import com.github.seepick.timesheet.contract.ContractDsl
import com.github.seepick.timesheet.contract.ContractDslImpl
import com.github.seepick.timesheet.contract.DefinedWorkContract
import com.github.seepick.timesheet.contract.WorkContract
import com.github.seepick.timesheet.contract.transformContracts
import com.github.seepick.timesheet.date.Day
import com.github.seepick.timesheet.date.TimeRangeSpec
import com.github.seepick.timesheet.date.WorkDay
import com.github.seepick.timesheet.date.toParsableDate
import com.github.seepick.timesheet.off.BuilderDayOffEntry
import com.github.seepick.timesheet.off.BuilderDaysOffEntry
import com.github.seepick.timesheet.off.DayOffDsl
import com.github.seepick.timesheet.off.OffReasons
import com.github.seepick.timesheet.off.ReasonableOffEntry
import com.github.seepick.timesheet.tags.TagDsl
import com.github.seepick.timesheet.tags.TagDslImpl
import com.github.seepick.timesheet.tags.Tags
import com.github.seepick.timesheet.timesheet.InvalidTimeEntryException
import com.github.seepick.timesheet.timesheet.OffReason
import com.github.seepick.timesheet.timesheet.TimeEntries
import com.github.seepick.timesheet.timesheet.TimeSheet
import java.time.LocalDate
import java.time.Month

class CurrentEntryHolder {
    lateinit var entry: BuilderEntry
}

class DslImplementation<TAGS : Tags, OFFS : OffReasons>(
    private val context: TimeSheetContext<TAGS, OFFS>,
    private val contractDslImpl: ContractDslImpl = ContractDslImpl(),
    private val currentEntryHolder: CurrentEntryHolder = CurrentEntryHolder(),
    private val tagDslImpl: TagDslImpl<TAGS, OFFS> = TagDslImpl(context, currentEntryHolder),
) :
    TimeSheetDsl,
    YearDsl,
    MonthDsl,
    ContractDsl by contractDslImpl,
    WorkDayDsl,
    DayOffDsl,
    TagDsl by tagDslImpl {

    private val entries = mutableListOf<BuilderEntry>()
    private lateinit var currentDay: LocalDate

    private var currentYear = -1 // Int not possible to do lateinit :-/
    private lateinit var currentMonth: Month

    private val contracts = mutableListOf<DefinedWorkContract>()

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
        currentEntryHolder.entry = BuilderDaysOffEntry(currentYear, currentMonth, days)
        entries += currentEntryHolder.entry
        return this
    }

    override fun DayOffDsl.becauseOf(reason: OffReason) {
        val entry = currentEntryHolder.entry as? ReasonableOffEntry
            ?: throw IllegalStateException("Expected entry to be reasonable, but was: ${currentEntryHolder.entry}")
        if (!context.offs.contains(reason)) {
            // TODO test whether contains off reason
        }
        entry.reason = reason
    }

    private fun _dayOff(newDate: LocalDate): DayOffDsl {
        currentDay = newDate // TODO needed to set?!
        currentEntryHolder.entry = BuilderDayOffEntry(currentDay)
        entries += currentEntryHolder.entry
        return this
    }

    // DAY DSL
    // ================================================================================================

    override infix fun String.about(description: String): TagDsl {
        val timeRangeSpec = TimeRangeSpec.parse(this)
        // overlap validation is done afterwards (as time ranges are built dynamically)
        currentEntryHolder.entry = BuilderWorkDayEntry(currentDay, timeRangeSpec, description)
        entries += currentEntryHolder.entry
        return this@DslImplementation
    }

    override operator fun String.minus(description: String) = about(description)


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
        if (contracts.isEmpty()) {
            contracts += DefinedWorkContract(WorkContract.default, realEntries.firstDate)
        }

        return TimeSheet(
            entries = realEntries,
            contracts = transformContracts(contracts, realEntries),
        )
    }

}
