@file:JvmName("BuilderTransformer") // for test recognition

package com.github.seepick.timesheet.dsl

import com.github.seepick.timesheet.date.ClosedRangeSpec
import com.github.seepick.timesheet.date.HasEndTime
import com.github.seepick.timesheet.date.HasStartTime
import com.github.seepick.timesheet.date.OpenEndRangeSpec
import com.github.seepick.timesheet.date.OpenStartRangeSpec
import com.github.seepick.timesheet.date.TimeRange
import com.github.seepick.timesheet.date.TimeRangeSpec
import com.github.seepick.timesheet.date.toParsableDate
import com.github.seepick.timesheet.off.BuilderDayOffEntry
import com.github.seepick.timesheet.off.BuilderDaysOffEntry
import com.github.seepick.timesheet.off.DayOffEntry
import com.github.seepick.timesheet.timesheet.EntryDateRange
import com.github.seepick.timesheet.timesheet.TimeEntry
import com.github.seepick.timesheet.timesheet.WorkDayEntry
import java.time.LocalDate

fun BuilderEntry.toRealEntry(neighbours: Pair<BuilderEntry?, BuilderEntry?>): List<TimeEntry> =
    when (this) {
        is BuilderWorkDayEntry -> listOf(
            WorkDayEntry(
                dateRange = EntryDateRange(
                    day = day,
                    timeRange = transformTimeRange(timeRangeSpec, neighbours, day)
                ),
                about = about,
                tags = tags
            )
        )

        is BuilderDayOffEntry -> listOf(
            DayOffEntry(
                day = day,
                reason = reason ?: throw InvalidSheetException("No day off reason was given for: $this")
            )
        )

        is BuilderDaysOffEntry -> {
            val nonNullReason = reason ?: throw InvalidSheetException("No day off reason was given for: $this")
            this.dates.map {
                DayOffEntry(day = it, reason = nonNullReason)
            }
        }

        else -> throw UnsupportedOperationException("Unrecognized BuilderEntry type: ${this.javaClass.name}")
    }

private fun transformTimeRange(
    timeRangeSpec: TimeRangeSpec,
    neighbours: Pair<BuilderEntry?, BuilderEntry?>,
    day: LocalDate
): TimeRange =
    when (timeRangeSpec) {
        is ClosedRangeSpec -> timeRangeSpec.toTimeRange()
        is OpenStartRangeSpec -> transformOpenAndEndRange(true, timeRangeSpec, neighbours, day)
        is OpenEndRangeSpec -> transformOpenAndEndRange(false, timeRangeSpec, neighbours, day)
    }

private fun transformOpenAndEndRange(
    isStartOpen: Boolean,
    timeRangeSpec: TimeRangeSpec,
    neighbours: Pair<BuilderEntry?, BuilderEntry?>,
    day: LocalDate
): TimeRange {
    val label = if (isStartOpen) "start" else "end"
    val labelInversed = if (isStartOpen) "end" else "start"
    val labelNeighbour = if (isStartOpen) "previous" else "following"
    val labelPrefix =
        "On ${day.toParsableDate()} an invalid open-$label-entry was created '${timeRangeSpec.toParseableString()}': "
    val neighbour = if (isStartOpen) neighbours.first else neighbours.second ?: {
        throw InvalidSheetException("$labelPrefix $labelNeighbour neighbour expected to EXIST!")
    }
    if (neighbour !is BuilderWorkDayEntry) {
        throw InvalidSheetException("$labelPrefix $labelNeighbour neighbour expected to be a WORK day!")
    }
    val requireType = if (isStartOpen) HasEndTime::class else HasStartTime::class
    if (!requireType.isInstance(neighbour.timeRangeSpec)) {
        throw InvalidSheetException("$labelPrefix $labelNeighbour neighbour expected $labelInversed TIME to be defined!")
    }
    return if (isStartOpen) {
        (timeRangeSpec as OpenStartRangeSpec).toTimeRange(start = (neighbour.timeRangeSpec as HasEndTime).end)
    } else {
        (timeRangeSpec as OpenEndRangeSpec).toTimeRange(end = (neighbour.timeRangeSpec as HasStartTime).start)
    }
}
