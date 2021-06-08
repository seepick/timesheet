package com.github.cpickl.timesheet

import java.time.LocalDate
import java.time.LocalTime

data class TimeSheet(
    val start: LocalDate,
    val weekHoursTarget: Int,
    val entries: List<TimeEntry>,
)

sealed class TimeEntry

data class WorkTimeEntry(
    val hours: EntryDateRange,
    val description: String,
    val tag: Tag,
) : TimeEntry()

enum class Tag {
    None,
    Organization,
    Meeting,
    Coding,
    Education,
}

data class EntryDateRange(
    val day: LocalDate,
    val range: TimeRange,
)

data class TimeRange(
    val start: LocalTime,
    val end: LocalTime,
)

data class OffTimeEntry(
    val day: LocalDate,
    val tag: OffTag,
) : TimeEntry()

enum class OffTag {
    Sick,
    PublicHoliday,
    Vacation,
}