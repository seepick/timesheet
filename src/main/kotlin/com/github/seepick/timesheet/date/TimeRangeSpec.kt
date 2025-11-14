package com.github.seepick.timesheet.date

import java.time.DateTimeException
import java.time.LocalTime

sealed interface TimeRangeSpec {

    companion object {

        private val regex = """^((\d{1,2})(:(\d{2}))?)?-((\d{1,2})(:(\d{2}))?)?${'$'}""".toRegex()

        fun parse(input: String): TimeRangeSpec {
            if (!regex.matches(input)) {
                throw TimeRangeSpecParseException("Didn't match expected format: [$input]!")
            }
            // input: "8:00-9:00"
            //               0          1     2  3    4   5     6  7    8
            // groupValues: [8:00-9:00, 8:00, 8, :00, 00, 9:00, 9, :00, 00]
            val matchResult = regex.find(input) ?: throw TimeRangeSpecParseException("Failed to parse time: [$input]!")
            val startPart = matchResult.groupValues[1]
            val endPart = matchResult.groupValues[5]

            return if (startPart.isNotEmpty() && endPart.isNotEmpty()) {
                val start = parseTimePart(startPart)
                val end = parseTimePart(endPart)
                if (!start.isBefore(end)) {
                    throw TimeRangeSpecParseException("Start ($startPart) must be before end ($endPart) for [$input]!")
                }
                ClosedRangeSpec(start, end)
            } else if (startPart.isNotEmpty()) {
                OpenEndRangeSpec(start = parseTimePart(startPart))
            } else if (endPart.isNotEmpty()) {
                OpenStartRangeSpec(end = parseTimePart(endPart))
            } else {
                throw TimeRangeSpecParseException("Must define either start or end time! For: [$input]")
            }
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
                throw TimeRangeSpecParseException("Failed to parse time input: [$input]!", dateException)
            }
    }

    fun toParseableString(): String
}

class TimeRangeSpecParseException(message: String, cause: Exception? = null) : Exception(message, cause)

/** Both defined: "8:00-9:00" */
data class ClosedRangeSpec(
    override val start: LocalTime,
    override val end: LocalTime,
) : TimeRangeSpec, HasStartTime, HasEndTime {
    init {
        require(start.isBefore(end)) { "Require start ($start) before end ($end)!" }
    }

    fun toTimeRange() = TimeRange(start = start, end = end)
    override fun toParseableString() = "$start-$end"
}

/** Start only defined: "8:00-" */
data class OpenEndRangeSpec(
    override val start: LocalTime
) : TimeRangeSpec, HasStartTime {
    fun toTimeRange(end: LocalTime) = TimeRange(start = start, end = end)
    override fun toParseableString() = "$start-"
}

/** End only defined: "-8:00" */
data class OpenStartRangeSpec(
    override val end: LocalTime
) : TimeRangeSpec, HasEndTime {
    fun toTimeRange(start: LocalTime) = TimeRange(start = start, end = end)
    override fun toParseableString() = "-$end"
}
