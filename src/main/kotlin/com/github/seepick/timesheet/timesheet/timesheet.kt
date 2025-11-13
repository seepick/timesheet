package com.github.seepick.timesheet.timesheet

import com.github.seepick.timesheet.contract.RangedWorkContract
import com.github.seepick.timesheet.contract.WorkContract
import com.github.seepick.timesheet.date.HasTimeRange
import com.github.seepick.timesheet.date.Minutes
import com.github.seepick.timesheet.date.TimeRange
import java.time.LocalDate

data class TimeSheet(
    val entries: TimeEntries,
    val contracts: List<RangedWorkContract>,
) {
    fun contractFor(date: LocalDate): WorkContract =
        contracts.find { it.isWithin(date) }?.contract ?: throw RuntimeException("No contract found for date [$date]")

    val startDate = entries.firstDate
    val endDate = entries.lastDate
}

data class EntryDateRange(
    val day: LocalDate,
    override val timeRange: TimeRange,
) : HasTimeRange {
    val duration: Minutes = timeRange.duration
}
