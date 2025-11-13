package com.github.seepick.timesheet.timesheet

import com.github.seepick.timesheet.contract.RangedWorkContract
import com.github.seepick.timesheet.date.HasTimeRange
import com.github.seepick.timesheet.date.Minutes
import com.github.seepick.timesheet.date.TimeRange
import java.time.LocalDate

data class TimeSheet(
    val entries: TimeEntries,
    val contracts: List<RangedWorkContract>,
) {
    val hoursToWorkPerDay = 8 // FIXME !!! change to: hours to work per week; based on that, calculate back per day
    val startDate = entries.firstDate
    val endDate = entries.lastDate
//    private val freeDaysJavaType = freeDays.map { it.allDays }

//    fun freeDaysContains(day: DayOfWeek) = freeDaysJavaType.contains(day)
}


data class EntryDateRange(
    val day: LocalDate,
    override val timeRange: TimeRange,
) : HasTimeRange {
    val duration: Minutes = timeRange.duration
}
