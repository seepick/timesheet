@file:JvmName("Model")

package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.toParseableString
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

typealias Minutes = Long

enum class WorkDay(val allDays: DayOfWeek) {
    Monday(DayOfWeek.MONDAY),
    Tuesday(DayOfWeek.TUESDAY),
    Wednesday(DayOfWeek.WEDNESDAY),
    Thursday(DayOfWeek.THURSDAY),
    Friday(DayOfWeek.FRIDAY),
}

data class TimeSheet(
    val freeDays: Set<WorkDay>,
    val entries: TimeEntries,
) {
    val hoursToWorkPerDay = 8
    val startDate: LocalDate = entries.firstDate
    private val freeDaysJavaType = freeDays.map { it.allDays }

    fun freeDaysContains(day: DayOfWeek) = freeDaysJavaType.contains(day)

}

data class TimeEntries(
    private val entries: List<TimeEntry>,
) : List<TimeEntry> by entries {
    init {
        require(entries.first() is WorkDayEntry) { "First entry must be a working day but was: ${entries.first()}" }
    }

    val firstDate: LocalDate = (entries.first() as WorkDayEntry).dateRange.day
    val workEntries = entries.filterIsInstance<WorkDayEntry>()
    val dayOffEntries = entries.filterIsInstance<DayOffEntry>()
}

sealed class TimeEntry : TimeEntryFields

interface TimeEntryFields {
    val day: LocalDate
}

data class WorkDayEntry(
    val dateRange: EntryDateRange,
    val about: String,
    val tag: Tag,
) : TimeEntry() {

    val duration: Minutes = dateRange.duration
    override val day: LocalDate = dateRange.day
}

enum class Tag {
    None,
    Business,
    Coding,
    Meeting,
    Organization,
    Education,
    Scrum,
}

data class EntryDateRange(
    val day: LocalDate,
    val range: TimeRange,
) {
    val duration: Minutes = range.duration
}

data class TimeRange(
    val start: LocalTime,
    val end: LocalTime,
) {
    init {
        require(start.isBefore(end))
    }

    val duration: Minutes = ChronoUnit.MINUTES.between(start, end)
    private val parseableString = "${start.toParseableString()}-${end.toParseableString()}"

    fun overlaps(other: TimeRange): Boolean {
        // MINOR can be improved
        return when {
            other.start == start && other.end == end -> true
            other.start.isAfter(start) && other.start.isBefore(end) -> true
            other.end.isAfter(start) && other.end.isBefore(end) -> true
            other.start.isAfter(start) && other.end.isBefore(end) -> true
            other.start.isBefore(start) && other.end.isAfter(end) -> true
            else -> false
        }
    }

    fun toParseableString() = parseableString
}


data class DayOffEntry(
    override val day: LocalDate,
    val tag: OffTag,
) : TimeEntry()

enum class OffTag {
    Sick,
    PublicHoliday,
    Vacation,
}