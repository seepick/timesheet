package com.github.seepick.timesheet.test_infra

import com.github.seepick.timesheet.date.TimeRange
import java.time.LocalDate
import java.time.LocalTime

object TestConstants {
    val someDate = LocalDate.of(2003, 2, 1)
    val date1 = someDate
    val date2 = date1.plusDays(1)

    val someTime = LocalTime.of(13, 37)
    val time1 = LocalTime.of(0, 0)
    val time2 = time1.plusHours(1)
    val time3 = time2.plusHours(1)
    val time4 = time3.plusHours(1)
    val someTimeRange = TimeRange(time1, time2)
    val timeRange1 = TimeRange(time1, time2)
    val timeRange2 = TimeRange(time3, time4)
}
