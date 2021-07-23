package com.github.cpickl.timesheet

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

    val firstDate: LocalDate = (entries.first() as WorkDayEntry).hours.day
    val workEntries = entries.filterIsInstance<WorkDayEntry>()
    val dayOffEntries = entries.filterIsInstance<DayOffEntry>()
}

sealed class TimeEntry

data class WorkDayEntry(
    val hours: EntryDateRange,
    val about: String,
    val tag: Tag,
) : TimeEntry() {

    val duration: Minutes = hours.duration

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
    val duration: Minutes = ChronoUnit.MINUTES.between(start, end)

    init {
        require(start.isBefore(end))
    }
}


data class DayOffEntry(
    val day: LocalDate,
    val tag: OffTag,
) : TimeEntry()

enum class OffTag {
    Sick,
    PublicHoliday,
    Vacation,
}