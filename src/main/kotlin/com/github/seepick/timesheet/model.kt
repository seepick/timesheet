@file:JvmName("Model")

package com.github.seepick.timesheet

import com.github.seepick.timesheet.builder.toParsableDate
import com.github.seepick.timesheet.builder.toParseableString
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

typealias Minutes = Long
typealias Hours = Double

enum class WorkDay(val javaDay: DayOfWeek, val day: Day) {
    monday(DayOfWeek.MONDAY, Day.monday),
    tuesday(DayOfWeek.TUESDAY, Day.tuesday),
    wednesday(DayOfWeek.WEDNESDAY, Day.wednesday),
    thursday(DayOfWeek.THURSDAY, Day.thursday),
    friday(DayOfWeek.FRIDAY, Day.friday),
}

enum class Day(val javaDay: DayOfWeek) {
    monday(DayOfWeek.MONDAY),
    tuesday(DayOfWeek.TUESDAY),
    wednesday(DayOfWeek.WEDNESDAY),
    thursday(DayOfWeek.THURSDAY),
    friday(DayOfWeek.FRIDAY),
    saturday(DayOfWeek.SATURDAY),
    sunday(DayOfWeek.SUNDAY),
}


data class TimeSheet(
    val entries: TimeEntries,
    val contracts: List<RangedWorkContract>,
) {
    val hoursToWorkPerDay = 8 // FIXME !!! change to: hours to work per week; based on that, calculate back per day
    val startDate: LocalDate = entries.firstDate
//    private val freeDaysJavaType = freeDays.map { it.allDays }

//    fun freeDaysContains(day: DayOfWeek) = freeDaysJavaType.contains(day)
}

class TimeEntries private constructor(
    private val entries: List<TimeEntry>,
) : List<TimeEntry> by entries {

    companion object {
        fun newValidatedOrThrow(entries: List<TimeEntry>): TimeEntries {
            if (entries.isEmpty()) {
                throw InputValidationException("Why you wanna try to build a timesheet without any entries? That has no sense, non-sense!")
            }
            if (entries.first() !is WorkDayEntry) {
                throw InputValidationException("First entry must be a working day but was: ${entries.first()}")
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
                    throw InputValidationException("Overlap in time for the day: ${day.toParsableDate()}!")
                }
            }
            return TimeEntries(entries)
        }
    }

    val firstDate: LocalDate = entries.first().day
    val lastDate: LocalDate = entries.last().day
    val workEntries = entries.filterIsInstance<WorkDayEntry>()
    val dayOffEntries = entries.filterIsInstance<DayOffEntry>()

    override fun toString() = "TimeEntries[entries=$entries]"
}

open class InputValidationException(message: String) : Exception(message)

sealed interface TimeEntry : TimeEntryFields

interface TimeEntryFields {
    val day: LocalDate
}

data class WorkDayEntry(
    val dateRange: EntryDateRange,
    val about: String,
    val tags: Set<Tag>,
) : TimeEntry, HasTimeRange by dateRange {

    val duration: Minutes = dateRange.duration
    override val day: LocalDate = dateRange.day

}

interface Tag {
    val label: String

    companion object;
}

class NamedTag(
    override val label: String
) : Tag


interface OffReason {
    val label: String
    companion object
}

class NamedOffReason(
    override val label: String
): OffReason

interface HasTimeRange {
    val timeRange: TimeRange

    fun overlaps(otherRange: HasTimeRange): Boolean =
        timeRange.overlaps(otherRange.timeRange)
}

data class EntryDateRange(
    val day: LocalDate,
    override val timeRange: TimeRange,
) : HasTimeRange {
    val duration: Minutes = timeRange.duration
}

/** inclusive **/
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
)

data class TimeRange(
    val start: LocalTime,
    val end: LocalTime,
) {
    init {
        require(start.isBefore(end))
    }

    val duration: Minutes = ChronoUnit.MINUTES.between(start, end)
    private val parseableString = "${start.toParseableString()}-${end.toParseableString()}"


    fun toParseableString() = parseableString

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
}

infix fun Int.until(m: Int) = TimeRange(LocalTime.of(this, 0), LocalTime.of(m, 0))

data class DayOffEntry(
    override val day: LocalDate,
    val reason: OffReason,
) : TimeEntry
