package com.github.seepick.timesheet

import com.github.seepick.timesheet.builder.TimeSheetDsl
import com.github.seepick.timesheet.builder.WorkDayDsl
import com.github.seepick.timesheet.builder.MonthDsl
import java.time.LocalDate
import java.time.Month

private val anyYear = 2000
private val anyMonth = Month.JANUARY

fun TimeSheetDsl.anyYearMonth(code: MonthDsl.() -> Unit) {
    year(anyYear) {
        month(anyMonth, code)
    }
}

fun TimeSheetDsl.dayOff(date: LocalDate = TestConstants.someDate, reason: OffReason) {
    year(date.year) {
        month(date.month) {
            dayOff(date.dayOfMonth) becauseOf reason
        }
    }
}

fun TimeSheetDsl.anyWorkingDay() = someWorkingDay()

fun TimeSheetDsl.someWorkingDay(date: LocalDate = TestConstants.someDate, dayDsl: WorkDayDsl.() -> Unit = { someWorkEntry() }) {
    year(date.year) {
        month(date.month) {
            day(date.dayOfMonth) {
                dayDsl()
            }
        }
    }
}

fun TimeSheetDsl.anyDayOff() = someDayOff()

fun TimeSheetDsl.someDayOff(date: LocalDate = TestConstants.someDate) {
    year(date.year) {
        month(date.month) {
            dayOff(date.dayOfMonth) becauseOf OffReason.any
        }
    }
}

fun MonthDsl.someWorkingDay(day: Int) {
    day(day) {
        someWorkEntry()
    }
}


fun WorkDayDsl.someWorkEntry(timeRange: String = someTimeRange, about: String = "some about") {
    timeRange about about
}

val WorkDayDsl.someTimeRange: String get() = "10-11"

fun MonthDsl.someDayOff(day: Int) {
    dayOff(day) becauseOf OffReason.any
}
