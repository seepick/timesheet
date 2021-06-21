package com.github.cpickl.timesheet

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun String.parseDate(): LocalDate = LocalDate.parse(this, DATE_FORMAT)
private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy")

fun TimeSheetDsl.fullDay(date: String) {
    day(date) {
        "10-18" about "any"
    }
}

fun TimeSheetDsl.someDayOff(date: String = "1.1.21") {
    dayOff(date) becauseOf DayOffReason.any
}

fun TimeSheetDsl.someWorkingDay(date: String = "1.1.21") {
    day(date) {
        "9-10" about "some" tag (IntermediateTag.any)
    }
}
