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

fun timesheet(initCode: TimeSheetInitDsl.() -> Unit = {}, entryCode: TimeSheetDsl.() -> Unit): TimeSheet {
    val dsl = DslImplementation()
    dsl.initCode()
    dsl.entryCode()
    return dsl.build()
}

interface TimeSheetInitDsl {
    var freeDays: MutableSet<WorkDay>
}

interface TimeSheetDsl {
    fun day(date: String, code: DayDsl.() -> Unit)
    fun dayOff(date: String): DayOffDsl

    infix fun DayOffDsl.becauseOf(reason: DayOffReasonDso)
}

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

private class DslImplementation : TimeSheetInitDsl, TimeSheetDsl, DayDsl, DayOffDsl, PostAboutDsl {

    override var freeDays = mutableSetOf<WorkDay>()

    private val entries = mutableListOf<IntermediateEntryDso>()
    private lateinit var currentDay: LocalDate
    private lateinit var currentEntry: IntermediateEntryDso

    override fun day(date: String, code: DayDsl.() -> Unit) {
        currentDay = date.parseDate()
        if (entries.any { it.day == currentDay }) {
            throw BuilderException("Duplicate date entries: $date")
        }

        code()
    }

    override fun dayOff(date: String): DayOffDsl {
        currentDay = date.parseDate()
        currentEntry = DayOffEntryDso(currentDay)
        entries += currentEntry
        return this
    }

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

    override operator fun String.minus(description: String) = about(description)

    override fun tag(tag: TagDso) {
        val entry = currentEntry as IntermediateWorkDayEntryDso
        entry.tag = tag
    }

    override operator fun minus(tag: TagDso) = tag(tag)

    override fun DayOffDsl.becauseOf(reason: DayOffReasonDso) {
        val entry = currentEntry as DayOffEntryDso
        entry.reason = reason
    }

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
            hours = EntryDateRange(
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
}
