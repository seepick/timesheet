package com.github.seepick.timesheet.dsl

import com.github.seepick.timesheet.contract.ContractDsl
import com.github.seepick.timesheet.contract.ContractDslImpl
import com.github.seepick.timesheet.contract.DefinedWorkContract
import com.github.seepick.timesheet.contract.WorkContract
import com.github.seepick.timesheet.contract.transformContracts
import com.github.seepick.timesheet.date.Clock
import com.github.seepick.timesheet.date.Day
import com.github.seepick.timesheet.date.TimeRangeSpec
import com.github.seepick.timesheet.date.WorkDay
import com.github.seepick.timesheet.date.toParsableDate
import com.github.seepick.timesheet.off.BuilderDayOffEntry
import com.github.seepick.timesheet.off.BuilderDaysOffEntry
import com.github.seepick.timesheet.off.DayOffDsl
import com.github.seepick.timesheet.off.OffReason
import com.github.seepick.timesheet.off.OffReasons
import com.github.seepick.timesheet.off.ReasonableOffEntry
import com.github.seepick.timesheet.tags.TagDsl
import com.github.seepick.timesheet.tags.TagDslImpl
import com.github.seepick.timesheet.tags.Tags
import com.github.seepick.timesheet.timesheet.TimeEntries
import com.github.seepick.timesheet.timesheet.TimeSheet
import java.time.LocalDate
import java.time.Month

class InvalidEntriesException(message: String) : RuntimeException(message)

class Current {
    var year = -1 // Int not possible to do lateinit :-/
    lateinit var month: Month
    lateinit var day: LocalDate
    lateinit var entry: BuilderEntry
    val entries = mutableListOf<BuilderEntry>()
    val contracts = mutableListOf<DefinedWorkContract>()

    fun addDefaultContractIfEmpty(contractDefinedAt: LocalDate) {
        if (contracts.isEmpty()) {
            contracts += DefinedWorkContract(WorkContract.default, contractDefinedAt)
        }
    }

    fun throwOnDuplicateDaysOffEntries() {
        entries.map { entry ->
            when (entry) {
                is BuilderDayOffEntry -> listOf(entry.day to entry)
                is BuilderDaysOffEntry -> entry.days.map { LocalDate.of(entry.year, entry.month, it) to entry }
                is BuilderWorkDayEntry -> listOf(entry.day to entry)
                else -> throw Exception("Unhandled BuilderEntry type: ${entry::class.qualifiedName}")
            }
        }.flatten().groupBy { it.first }
            .filter { it.value.size >= 2 && !it.value.map { it.second }.all { it is BuilderWorkDayEntry } }
            .also { duplicates ->
                if (duplicates.isNotEmpty()) {
                    throw InvalidEntriesException("Found duplicates: $duplicates")
                }
            }
    }
}

class DslImplementation<TAGS : Tags, OFFS : OffReasons>(
    private val context: TimeSheetContext<TAGS, OFFS>,
    private val clock: Clock,
    private val contractDslImpl: ContractDslImpl = ContractDslImpl(),
    private val current: Current = Current(),
    private val tagDslImpl: TagDslImpl<TAGS, OFFS> = TagDslImpl(context, current),
) : TimeSheetDsl, YearDsl, MonthDsl, WorkDayDsl, DayOffDsl, ContractDsl by contractDslImpl, TagDsl by tagDslImpl {

    // MAIN TIMESHEET DSL
    // ================================================================================================

    override fun year(year: Int, code: YearDsl.() -> Unit) {
        current.year = year
        code()
    }

    override fun month(month: Month, code: MonthDsl.() -> Unit) {
        current.month = month
        code()
    }

    override fun month(month: Int, code: MonthDsl.() -> Unit) {
        month(Month.of(month), code)
    }

    override fun day(day: Int, code: WorkDayDsl.() -> Unit) {
        val newDate = dateByCurrentSetYearAndMonth(day)
        if (current.entries.any { it.matches(newDate) }) {
            throw InvalidSheetException("Duplicate date entries: ${newDate.toParsableDate()}")
        }
        current.day = newDate
        code()
    }

    override fun day(dayLabel: Day, day: Int, code: WorkDayDsl.() -> Unit) {
        verifyDayLabel(dayLabel, day)
        day(day, code)
    }

    private fun dateByCurrentSetYearAndMonth(day: Int) = LocalDate.of(current.year, current.month, day)

    private fun verifyDayLabel(dayLabel: Day, day: Int) {
        val currentDate = LocalDate.of(current.year, current.month, day)
        if (currentDate.dayOfWeek != dayLabel.javaDay) {
            throw IllegalArgumentException("Current date [$currentDate] with day [${currentDate.dayOfWeek}] mismatches expected [$dayLabel]!")
        }
    }

    // DAY DSL
    // ================================================================================================

    override fun contract(code: ContractDsl.() -> Unit) {
        code()
        current.contracts += DefinedWorkContract(
            contract = WorkContract(daysOff = daysOff, hoursPerWeek = hoursPerWeek), definedAt = current.day
        )
    }

    override infix fun String.about(description: String): TagDsl {
        val timeRangeSpec = TimeRangeSpec.parse(this)
        // overlap validation is done afterwards (as time ranges are built dynamically)
        current.entry = BuilderWorkDayEntry(current.day, timeRangeSpec, description)
        current.entries += current.entry
        return this@DslImplementation
    }

    override operator fun String.minus(description: String) = about(description)

    // DAY OFF DSL
    // ================================================================================================

    override fun dayOff(day: Int): DayOffDsl = internalDayOff(dateByCurrentSetYearAndMonth(day))

    override fun dayOff(dayLabel: WorkDay, day: Int): DayOffDsl {
        verifyDayLabel(dayLabel, day)
        return dayOff(day)
    }

    override fun daysOff(days: IntRange): DayOffDsl {
        current.entry = BuilderDaysOffEntry(current.year, current.month, days)
        current.entries += current.entry
        return this
    }

    override fun DayOffDsl.becauseOf(reason: OffReason) {
        val entry = current.entry as? ReasonableOffEntry
            ?: throw IllegalStateException("Expected entry to be reasonable, but was: ${current.entry}")
        if (!context.offs.contains(reason)) {
            // TODO test whether contains off reason
        }
        entry.reason = reason
    }

    private fun internalDayOff(newDate: LocalDate): DayOffDsl {
        current.day = newDate // TODO needed to set?!
        current.entry = BuilderDayOffEntry(current.day)
        current.entries += current.entry
        return this
    }

    // BUILD
    // ================================================================================================

    fun build(): TimeSheet {
        if (current.entries.isEmpty()) {
            throw InvalidSheetException("Sheet requires at least one entry to be valid.")
        }
        try {
            current.throwOnDuplicateDaysOffEntries()
        } catch (e: InvalidEntriesException) {
            throw InvalidSheetException("Invalid entries found!", e)
        }
        val timeEntries = TimeEntries.byBuilderEntries(current.entries)
        // TODO throwOnOverlappingWorkTimes;
        // filter WorkDayEntry only; group by day
        // ensure order: if not all ordered asc, then fail
        // validate by checking no overlap (== is ok, but not left's start > right's start)
        current.addDefaultContractIfEmpty(timeEntries.firstDate)
        val today = clock.currentLocalDate()
        if (timeEntries.lastDate > today) {
            throw InvalidSheetException("Entries (last date ${timeEntries.lastDate}) must not be in the future (today ${today})!")
        }
        return TimeSheet(
            entries = timeEntries,
            contracts = transformContracts(
                contracts = current.contracts,
                sheetEndDate = today,
            ),
        )
    }
}

class InvalidSheetException(message: String, cause: Exception? = null) : Exception(message, cause)
