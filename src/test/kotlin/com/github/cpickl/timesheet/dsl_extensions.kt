package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.TimeSheetDsl
import com.github.cpickl.timesheet.builder.WorkDayDsl
import com.github.cpickl.timesheet.builder.YearMonthDsl
import com.github.cpickl.timesheet.builder.toParsableDate
import java.time.LocalDate

fun TimeSheetDsl.someDayOff(date: String = "1.1.21") {
    dayOff(date) becauseOf OffReason.any
}

fun TimeSheetDsl.someWorkingDate(date: LocalDate = TestConstants.someDate, dayDsl: WorkDayDsl.() -> Unit = { someWorkEntry() }) {
    someWorkingDay(date.toParsableDate(), dayDsl)
}

@Deprecated(message = "Use year/month/day approach instead")
fun TimeSheetDsl.someWorkingDay(date: String = "1.1.21", dayDsl: WorkDayDsl.() -> Unit = { someWorkEntry() }) {
//    year(2021) {
//        month(8) {
//            day()
//        }
//    }
    day(date) {
        dayDsl()
    }
}

fun WorkDayDsl.someWorkEntry(timeRange: String = someTimeRange, about: String = "some about") {
    timeRange about about
}

val WorkDayDsl.someTimeRange: String get() = "10-11"

fun TimeSheetDsl.fullWorkingDay(date: String) {
    day(date) {
        "10-18" about "any"
    }
}

fun YearMonthDsl.someDayOff(day: Int) {
    dayOff(day) becauseOf OffReason.any
}
