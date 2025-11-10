package com.github.seepick.timesheet.date

import java.time.LocalTime

typealias Minutes = Long
typealias Hours = Double

interface HasStartTime {
    val start: LocalTime
}

interface HasEndTime {
    val end: LocalTime
}

infix fun Int.until(m: Int) = TimeRange(LocalTime.of(this, 0), LocalTime.of(m, 0))
