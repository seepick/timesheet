package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.OffReasons
import com.github.cpickl.timesheet.builder.Tags
import com.github.cpickl.timesheet.builder.TimeSheetDsl
import com.github.cpickl.timesheet.builder.TimeSheetInitDsl
import com.github.cpickl.timesheet.builder.timesheet
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun timesheet(init: TimeSheetInitDsl.() -> Unit = {}, entryCode: TimeSheetDsl.() -> Unit): TimeSheet =
    timesheet(Tags.any, OffReasons.any, init, entryCode)

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
