package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.DayDsl
import com.github.cpickl.timesheet.builder.DayOffReasonDso
import com.github.cpickl.timesheet.builder.TimeSheetDsl
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy")

fun String.parseDate(): LocalDate = LocalDate.parse(this, DATE_FORMAT)

fun DayDsl.anyEntry() {
    someTime about "anyAbout"
}

val DayDsl.someTime: String get() = "10-11"

fun TimeSheetDsl.fullWorkingDay(date: String) {
    day(date) {
        "10-18" about "any"
    }
}

fun TimeSheetDsl.someDayOff(date: String = "1.1.21") {
    dayOff(date) becauseOf DayOffReasonDso.any
}

fun TimeSheetDsl.someWorkingDay(date: String = "1.1.21", dayDsl: DayDsl.() -> Unit = { anyEntry() }) {
    day(date) {
        dayDsl()
    }
}

