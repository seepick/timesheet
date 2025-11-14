package com.github.seepick.timesheet.date

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

interface HasTimeRange {
    val timeRange: TimeRange

    fun overlaps(otherRange: HasTimeRange): Boolean =
        timeRange.overlaps(otherRange.timeRange)
}

data class TimeRange(
    val start: LocalTime,
    val end: LocalTime,
) {
    companion object {
        private val TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm")
    }

    init {
        require(start.isBefore(end)) { "start [$start] must be before end [$end]" }
    }

    val formatted by lazy {
        TIME_FORMAT.format(start) + "-" + TIME_FORMAT.format(end)
    }

    val duration: Minutes = ChronoUnit.MINUTES.between(start, end)
    private val parseableString = "${start.toParseableString()}-${end.toParseableString()}"

    fun toParseableString() = parseableString

    fun overlaps(other: TimeRange): Boolean = when {
        other.start == start && other.end == end -> true
        other.start.isAfter(start) && other.start.isBefore(end) -> true
        other.end.isAfter(start) && other.end.isBefore(end) -> true
        other.start.isAfter(start) && other.end.isBefore(end) -> true
        other.start.isBefore(start) && other.end.isAfter(end) -> true
        else -> false
    }
}
