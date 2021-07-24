package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.WorkDayDsl
import com.github.cpickl.timesheet.builder.DayOffReasonDso
import com.github.cpickl.timesheet.builder.Tags
import com.github.cpickl.timesheet.builder.TimeSheetDsl
import com.github.cpickl.timesheet.builder.TimeSheetInitDsl
import com.github.cpickl.timesheet.builder.timesheet
import com.github.cpickl.timesheet.builder.toParsableDate
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun timesheet(init: TimeSheetInitDsl.() -> Unit = {}, entryCode: TimeSheetDsl.() -> Unit): TimeSheet =
    timesheet(tags = Tags.any, init, entryCode)

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

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy")

fun String.parseDate(): LocalDate = LocalDate.parse(this, DATE_FORMAT)

fun WorkDayDsl.someWorkEntry(timeRange: String = someTimeRange, about: String = "some about") {
    timeRange about about
}

val WorkDayDsl.someTimeRange: String get() = "10-11"

fun TimeSheetDsl.fullWorkingDay(date: String) {
    day(date) {
        "10-18" about "any"
    }
}

fun TimeSheetDsl.someDayOff(date: String = "1.1.21") {
    dayOff(date) becauseOf DayOffReasonDso.any
}

fun TimeSheetDsl.someWorkingDate(date: LocalDate = TestConstants.someDate, dayDsl: WorkDayDsl.() -> Unit = { someWorkEntry() }) {
    someWorkingDay(date.toParsableDate(), dayDsl)
}

fun TimeSheetDsl.someWorkingDay(date: String = "1.1.21", dayDsl: WorkDayDsl.() -> Unit = { someWorkEntry() }) {
    day(date) {
        dayDsl()
    }
}

