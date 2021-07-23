@file:JvmName("Date")

package com.github.cpickl.timesheet.builder

import com.github.cpickl.timesheet.TimeRange
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy")

fun LocalDate.toParsableDate() = "$dayOfMonth.$monthValue.${year.toString().substring(2)}"

fun LocalTime.toParseableString() = toString()

fun String.parseDate(): LocalDate = LocalDate.parse(this, DATE_FORMAT)

fun String.parseTimeRange(): TimeRange {
    val parts = split("-")
    require(parts.size == 2) { "Invalid time parts: [$this]! Expected something like [14:00-15:30]."}
    return TimeRange(parseTimePart(parts[0]), parseTimePart(parts[1]))
}

private fun parseTimePart(part: String): LocalTime =
    if (part.contains(":")) {
        val hourMinute = part.split(":")
        LocalTime.of(hourMinute[0].toInt(), hourMinute[1].toInt())
    } else {
        LocalTime.of(part.toInt(), 0)
    }
