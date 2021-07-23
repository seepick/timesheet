package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.DayDsl
import com.github.cpickl.timesheet.builder.DayOffReasonDso
import com.github.cpickl.timesheet.builder.TimeSheetDsl
import com.github.cpickl.timesheet.builder.toParsableDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TestConstants{
    val anyDate = LocalDate.of(2003, 2, 1)
}

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy")

fun String.parseDate(): LocalDate = LocalDate.parse(this, DATE_FORMAT)

fun DayDsl.anyWorkEntry() {
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

fun TimeSheetDsl.someWorkingDate(date: LocalDate = TestConstants.anyDate, dayDsl: DayDsl.() -> Unit = { anyWorkEntry() }) {
    someWorkingDay(date.toParsableDate(), dayDsl)
}

fun TimeSheetDsl.someWorkingDay(date: String = "1.1.21", dayDsl: DayDsl.() -> Unit = { anyWorkEntry() }) {
    day(date) {
        dayDsl()
    }
}

