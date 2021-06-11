package com.github.cpickl.timesheet

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

typealias Minutes = Long

enum class WorkDay(val javaDay: DayOfWeek) {
    Monday(DayOfWeek.MONDAY),
    Tuesday(DayOfWeek.TUESDAY),
    Wednesday(DayOfWeek.WEDNESDAY),
    Thursday(DayOfWeek.THURSDAY),
    Friday(DayOfWeek.FRIDAY),
}

data class TimeSheet(
    val daysOff: Set<WorkDay>,
    val entries: TimeEntries,
) {
    val hoursToWorkPerDay = 8
    val startDate: LocalDate = entries.firstDate
    private val javaDaysOff = daysOff.map { it.javaDay }

    fun daysOffContains(day: DayOfWeek) = javaDaysOff.contains(day)

}

data class TimeEntries(
    private val entries: List<TimeEntry>,
) {
    init {
        require(entries.first() is WorkDayEntry)
    }

    val firstDate: LocalDate = (entries.first() as WorkDayEntry).hours.day
    val workEntries = entries.filterIsInstance<WorkDayEntry>()
}

sealed class TimeEntry

data class WorkDayEntry(
    val hours: EntryDateRange,
    val description: String,
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