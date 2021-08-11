@file:JvmName("Model")

package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.toParsableDate
import com.github.cpickl.timesheet.builder.toParseableString
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

typealias Minutes = Long
typealias Hours = Double

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

    val firstDate: LocalDate = (entries.first() as WorkDayEntry).dateRange.day
    val workEntries = entries.filterIsInstance<WorkDayEntry>()
    val dayOffEntries = entries.filterIsInstance<DayOffEntry>()

    override fun toString() = "TimeEntries[entries=$entries]"
}

open class InputValidationException(message: String) : Exception(message)

sealed class TimeEntry : TimeEntryFields

interface TimeEntryFields {
    val day: LocalDate
}

data class WorkDayEntry(
    val dateRange: EntryDateRange,
    val about: String,
    val tags: Set<Tag>,
) : TimeEntry(), HasTimeRange by dateRange {

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
) : TimeEntry()
