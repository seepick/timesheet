package com.github.seepick.timesheet.dsl.timesheet

import com.github.seepick.timesheet.date.StaticClock
import com.github.seepick.timesheet.dsl.BuilderException
import com.github.seepick.timesheet.dsl.MonthDsl
import com.github.seepick.timesheet.dsl.TimeSheetDsl
import com.github.seepick.timesheet.dsl.WorkDayDsl
import com.github.seepick.timesheet.dsl.timesheet
import com.github.seepick.timesheet.off.OffReason
import com.github.seepick.timesheet.off.OffReasons
import com.github.seepick.timesheet.off.any
import com.github.seepick.timesheet.tags.Tags
import com.github.seepick.timesheet.tags.any
import com.github.seepick.timesheet.test_infra.TestConstants
import com.github.seepick.timesheet.timesheet.TimeSheet
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.Month

private val anyYear = 2000
private val anyMonth = Month.JANUARY

fun timesheetAny(today: String, entryCode: TimeSheetDsl.() -> Unit): TimeSheet =
    timesheet(Tags.any, OffReasons.any, StaticClock(today), entryCode)

fun timesheetAny(today: LocalDate, entryCode: TimeSheetDsl.() -> Unit): TimeSheet =
    timesheet(Tags.any, OffReasons.any, StaticClock(today), entryCode)

fun failingTimesheet(today: LocalDate, dsl: TimeSheetDsl.() -> Unit): BuilderException =
    shouldThrow {
        timesheetAny(today, entryCode = dsl)
    }

infix fun TimeSheet.shouldHaveSingleEntryWithDate(expected: LocalDate) {
    entries.size shouldBe 1
    entries.first().day shouldBe expected
}

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

fun TimeSheetDsl.someWorkingDay(
    date: LocalDate = TestConstants.someDate,
    dayDsl: WorkDayDsl.() -> Unit = { someWorkEntry() }
) {
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

const val someTimeRange = "10-11"

fun MonthDsl.someDayOff(day: Int) {
    dayOff(day) becauseOf OffReason.any
}
