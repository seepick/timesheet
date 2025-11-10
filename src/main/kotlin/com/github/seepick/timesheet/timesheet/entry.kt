package com.github.seepick.timesheet.timesheet

import com.github.seepick.timesheet.date.HasTimeRange
import com.github.seepick.timesheet.date.Minutes
import com.github.seepick.timesheet.date.toParsableDate
import com.github.seepick.timesheet.dsl.BuilderEntry
import com.github.seepick.timesheet.dsl.BuilderException
import com.github.seepick.timesheet.dsl.toRealEntry
import com.github.seepick.timesheet.tags.Tag
import java.time.LocalDate

interface TimeEntryFields {
    val day: LocalDate
}

sealed interface TimeEntry : TimeEntryFields

data class WorkDayEntry(
    val dateRange: EntryDateRange,
    val about: String,
    val tags: Set<Tag>,
) : TimeEntry, HasTimeRange by dateRange {

    val duration: Minutes = dateRange.duration
    override val day: LocalDate = dateRange.day
}

class TimeEntries(
    private val entries: List<TimeEntry>,
) : List<TimeEntry> by entries {

    companion object {
        fun byBuilderEntries(entries: List<BuilderEntry>) = try {
            byTimeEntries(entries.mapIndexed { i, entry ->
                entry.toRealEntry(neighbours = entries.getOrNull(i - 1) to entries.getOrNull(i + 1))
            }.flatten())
        } catch (e: InvalidTimeEntryException) {
            throw BuilderException("Invalid timesheet defined: ${e.message}", e)
        }
    }

    val firstDate: LocalDate = entries.first().day
    val lastDate: LocalDate = entries.last().day
    val workEntries = entries.filterIsInstance<WorkDayEntry>()
    val dayOffEntries = entries.filterIsInstance<DayOffEntry>()

    override fun toString() = "TimeEntries[entries=$entries]"
}

class InvalidTimeEntryException(message: String) : Exception(message)


fun TimeEntries.Companion.byTimeEntries(entries: List<TimeEntry>): TimeEntries {
    if (entries.isEmpty()) {
        throw InvalidTimeEntryException("Why you wanna try to build a timesheet without any entries? That has no sense, non-sense!")
    }
    if (entries.first() !is WorkDayEntry) {
        throw InvalidTimeEntryException("First entry must be a working day but was: ${entries.first()}")
    }
    entries.filterIsInstance<WorkDayEntry>().groupBy { it.dateRange.day }.forEach { day, dayEntries ->
        val isInvalid = dayEntries.foldIndexed(false) { i, acc, entry ->
            when {
                acc -> acc // already invalid, just continue
                i == dayEntries.size - 1 -> false // last entry has nothing to overlap with, continue+done
                else -> entry.overlaps(dayEntries[i + 1])
            }
        }
        if (isInvalid) {
            throw InvalidTimeEntryException("Overlap in time for the day: ${day.toParsableDate()}!")
        }
    }
    return TimeEntries(entries)
}
