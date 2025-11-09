@file:JvmName("Date")

package com.github.seepick.timesheet.builder

import com.github.seepick.timesheet.TimeRange
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy")

fun LocalDate.toParsableDate() = "$dayOfMonth.$monthValue.${year.toString().substring(2)}"

fun LocalTime.toParseableString() = toString()

fun String.parseDate(): LocalDate = LocalDate.parse(this, DATE_FORMAT)

interface HasStartTime {
    val start: LocalTime
}

interface HasEndTime {
    val end: LocalTime
}

class TimeParseException(message: String, cause: Exception? = null) : Exception(message, cause)

sealed class TimeRangeSpec {

    companion object {
        private val regex = """^((\d{1,2})(:(\d{2}))?)?\-((\d{1,2})(:(\d{2}))?)?${'$'}""".toRegex()
        fun parse(input: String): TimeRangeSpec {
            if(!regex.matches(input)) {
                throw TimeParseException("Didn't match expected format: [$input]!")
            }
            // input: "8:00-9:00"
            //               0          1     2  3    4   5     6  7    8
            // groupValues: [8:00-9:00, 8:00, 8, :00, 00, 9:00, 9, :00, 00]
            val matchResult = regex.find(input) ?: throw TimeParseException("Failed to parse time: [$input]!")
            val startPart = matchResult.groupValues[1]
            val endPart = matchResult.groupValues[5]

            val result = if(startPart.isNotEmpty() && endPart.isNotEmpty()) {
                val start = parseTimePart(startPart)
                val end = parseTimePart(endPart)
                // TODO also check start is before when dynamically build
                if(!start.isBefore(end)) {
                    throw TimeParseException("Start ($startPart) must be before end ($endPart) for [$input]!")
                }
                ClosedRangeSpec(start, end)
            } else if (startPart.isNotEmpty()) {
                OpenEndRangeSpec(start = parseTimePart(startPart))
            } else if (endPart.isNotEmpty()) {
                OpenStartRangeSpec(end=parseTimePart(endPart))
            } else {
                throw TimeParseException("Must define either start or end time! For: [$input]")
            }
            return result
        }

        private fun parseTimePart(input: String): LocalTime =
            try {
                if (input.contains(":")) {
                    val hourMinute = input.split(":")
                    LocalTime.of(hourMinute[0].toInt(), hourMinute[1].toInt())
                } else {
                    LocalTime.of(input.toInt(), 0)
                }
            } catch (dateException: DateTimeException) {
                throw TimeParseException("Failed to parse time input: [$input]!", dateException)
            }
    }

    data class ClosedRangeSpec(
        override val start: LocalTime,
        override val end: LocalTime,
    ) : TimeRangeSpec(), HasStartTime, HasEndTime {
        init {
            require(start.isBefore(end)) { "Require start ($start) before end ($end)!"}
        }
        fun toTimeRange() = TimeRange(start = start, end = end)
        override fun toParseableString() = "$start-$end"
    }

    data class OpenEndRangeSpec(
        override val start: LocalTime
    ) : TimeRangeSpec(), HasStartTime {
        fun toTimeRange(end: LocalTime) = TimeRange(start = start, end = end)
        override fun toParseableString() = "$start-"
    }

    data class OpenStartRangeSpec(
        override val end: LocalTime
    ) : TimeRangeSpec(), HasEndTime {
        fun toTimeRange(start: LocalTime) = TimeRange(start = start, end = end)
        override fun toParseableString() = "-$end"
    }

    abstract fun toParseableString(): String
}
