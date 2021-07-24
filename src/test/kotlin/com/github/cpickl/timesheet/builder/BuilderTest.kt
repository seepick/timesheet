package com.github.cpickl.timesheet.builder

import com.github.cpickl.timesheet.DayOffEntry
import com.github.cpickl.timesheet.EntryDateRange
import com.github.cpickl.timesheet.OffTag
import com.github.cpickl.timesheet.Tag
import com.github.cpickl.timesheet.TestConstants
import com.github.cpickl.timesheet.TimeEntries
import com.github.cpickl.timesheet.TimeRange
import com.github.cpickl.timesheet.TimeSheet
import com.github.cpickl.timesheet.WorkDayEntry
import com.github.cpickl.timesheet.any
import com.github.cpickl.timesheet.someWorkEntry
import com.github.cpickl.timesheet.someDayOff
import com.github.cpickl.timesheet.someWorkingDate
import com.github.cpickl.timesheet.someWorkingDay
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month

class BuilderTest : DescribeSpec({

    val someDate = TestConstants.someDate
    val date1 = TestConstants.date1
    val date2 = TestConstants.date2
    val someTimeRange = TestConstants.someTimeRange
    val someTimeRangeString = someTimeRange.toParseableString()
    val anyTimeRangeString = someTimeRange.toParseableString()
    val timeRange1 = TestConstants.timeRange1
    val timeRange2 = TestConstants.timeRange2
    val description = "test description"
    val anyYear = 2010
    val anyMonth = Month.JULY

    describe("When sunshine case") {
        it("valid working day and entry Then sheet's start date is of work entry") {
            val sheet = timesheet {
                someWorkingDate(someDate) {
                    someWorkEntry()
                }
            }

            sheet.startDate shouldBe someDate
        }
        it("two valid working days Then two entries returned") {
            val sheet = timesheet {
                someWorkingDate {
                    someWorkEntry(timeRange = timeRange1.toParseableString())
                    someWorkEntry(timeRange = timeRange2.toParseableString())
                }
            }
            sheet.entries.size shouldBe 2
        }
        it("valid work day Then parsed entry returned") {
            val timeStart = LocalTime.of(9, 30)
            val timeEnd = LocalTime.of(10, 0)

            val sheet = timesheet {
                day(someDate.toParsableDate()) {
                    "9:30-10" about description tag (TagDso.meet)
                }
            }

            sheet.entries shouldBe TimeEntries(
                listOf(
                    WorkDayEntry(
                        dateRange = EntryDateRange(someDate, TimeRange(timeStart, timeEnd)),
                        about = description,
                        tag = Tag.Meeting,
                    )
                )
            )
        }
        it("day off") {
            val sheet = timesheet {
                someWorkingDate(date1)
                dayOff(date2.toParsableDate()) becauseOf DayOffReasonDso.PublicHoliday
            }

            sheet.entries shouldContain DayOffEntry(
                day = date2,
                tag = OffTag.PublicHoliday,
            )
        }
    }
    describe("When ... invalid Then fail") {
        it("no days") {
            failingTimesheet {}
        }
        it("starts with day-off day") {
            failingTimesheet {
                someDayOff()
            }
        }
        it("Given some work day When day-off without reason entry") {
            failingTimesheet {
                someWorkingDay()
                dayOff("1.1.00") // missing: becauseOf tag
            }
        }
        it("two work days with same date") {
            failingTimesheet {
                val conflictingDate = "1.1.21"
                someWorkingDay(date = conflictingDate)
                someWorkingDay(date = conflictingDate)

            }.message shouldContain "1.1.21"
        }
        // TODO two day offs with same date
        // TODO 1 work day 1 day off; same date
        it("work entry without about") {
            failingTimesheet {
                someWorkingDate(someDate) {
                    anyTimeRangeString about " "
                }
            }.message shouldContain someDate.toParsableDate()
        }

        it("two work entries with same time") {
            failingTimesheet {
                someWorkingDate(someDate) {
                    someWorkEntry(timeRange = someTimeRangeString)
                    someWorkEntry(timeRange = someTimeRangeString)
                }
            }.message shouldContain someTimeRangeString
        }
    }

    describe("When ... year-month-day") {
        it("When add work-day Then set date correctly") {
            timesheet {
                year(2003) {
                    month(Month.of(2)) {
                        day(1) {
                            someWorkEntry()
                        }
                    }
                }
            } shouldHaveSingleEntryWithDate LocalDate.of(2003, 2, 1)
        }
        it("Given work-day When add day-off Then set date correctly") {
            val day1 = 1
            val day2 = 2

            val sheet = timesheet {
                year(anyYear) {
                    month(anyMonth) {
                        day(day1) {
                            someWorkEntry()
                        }
                        someDayOff(day2)
                    }
                }
            }

            sheet.entries.size shouldBe 2
            sheet.entries.last().day.dayOfMonth shouldBe day2
        }
    }

    describe("When ... year-month-day combined with manual-oldschool") {
        it("Given oldschool entry When add newschool entry Then both co-exist") {
            val sheet = timesheet {
                // oldschool
                day("1.1.00") {
                    someWorkEntry()
                }
                // newschool
                year(2000) {
                    month(Month.of(1)) {
                        day(2) {
                            someWorkEntry()
                        }
                    }
                }
            }

            sheet.entries.size shouldBe 2
            sheet.entries.first().day shouldBe LocalDate.of(2000,1,1)
            sheet.entries.last().day shouldBe LocalDate.of(2000,1,2)
        }
    }
})

fun YearMonthDsl.someDayOff(day: Int) {
    dayOff(day) becauseOf DayOffReasonDso.any
}

infix fun TimeSheet.shouldHaveSingleEntryWithDate(expected: LocalDate) {
    entries.size shouldBe 1
    entries.first().day shouldBe expected
}

fun failingTimesheet(dsl: TimeSheetDsl.() -> Unit): BuilderException =
    shouldThrow {
        timesheet(entryCode = dsl)
    }

