package com.github.seepick.timesheet.builder

import com.github.seepick.timesheet.DateRange
import com.github.seepick.timesheet.DayOffEntry
import com.github.seepick.timesheet.EntryDateRange
import com.github.seepick.timesheet.OffReason
import com.github.seepick.timesheet.Tag
import com.github.seepick.timesheet.TestConstants
import com.github.seepick.timesheet.TimeEntries
import com.github.seepick.timesheet.TimeRange
import com.github.seepick.timesheet.WorkContract
import com.github.seepick.timesheet.WorkDay.friday
import com.github.seepick.timesheet.WorkDay.monday
import com.github.seepick.timesheet.WorkDayEntry
import com.github.seepick.timesheet.until
import com.github.seepick.timesheet.any
import com.github.seepick.timesheet.anyWorkingDay
import com.github.seepick.timesheet.dayOff
import com.github.seepick.timesheet.failingTimesheet
import com.github.seepick.timesheet.shouldHaveSingleEntryWithDate
import com.github.seepick.timesheet.someWorkEntry
import com.github.seepick.timesheet.someDayOff
import com.github.seepick.timesheet.someWorkingDay
import com.github.seepick.timesheet.tag1
import com.github.seepick.timesheet.tag2
import com.github.seepick.timesheet.timesheetAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.Month.JULY
import java.time.Month.NOVEMBER
import java.time.Month.SEPTEMBER

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
    val anyDescription = "any description"
    val anyYear = 2010
    val anyMonth = JULY
    val someTag = Tag.any
    val tag1 = Tag.tag1
    val tag2 = Tag.tag2
    val someOffReason = OffReason.any

    describe("When sunshine case") {
        it("valid working day and entry Then sheet's start date is of work entry") {
            val sheet = timesheetAny {
                someWorkingDay(someDate) {
                    someWorkEntry()
                }
            }

            sheet.startDate shouldBe someDate
        }
        it("two valid working days Then two entries returned") {
            val sheet = timesheetAny {
                someWorkingDay {
                    someWorkEntry(timeRange = timeRange1.toParseableString())
                    someWorkEntry(timeRange = timeRange2.toParseableString())
                }
            }
            sheet.entries.size shouldBe 2
        }
        it("valid work day Then parsed entry returned") {
            val timeStart = LocalTime.of(9, 30)
            val timeEnd = LocalTime.of(10, 0)

            val sheet = timesheetAny {
                someWorkingDay(someDate) {
                    "9:30-10" about description tag (someTag)
                }
            }

            sheet.entries shouldBe TimeEntries.newValidatedOrThrow(
                listOf(
                    WorkDayEntry(
                        dateRange = EntryDateRange(someDate, TimeRange(timeStart, timeEnd)),
                        about = description,
                        tags = setOf(someTag),
                    )
                )
            )
        }
        it("two tags Then parsed tags returned") {
            val sheet = timesheetAny {
                someWorkingDay {
                    anyTimeRangeString - anyDescription - listOf(tag1, tag2)
                    // anyTimeRangeString.about(anyDescription).tags(tag1, tag2)
                }
            }

            sheet.entries.workEntries shouldHaveSize 1
            sheet.entries.workEntries[0].tags shouldContainExactly setOf(tag1, tag2)
        }
        it("day off") {
            val sheet = timesheetAny {
                anyWorkingDay()
                dayOff(date2, someOffReason)
            }

            sheet.entries shouldContain DayOffEntry(
                day = date2,
                reason = someOffReason,
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
                anyWorkingDay()
                year(anyYear) {
                    month(anyMonth) {
                        dayOff(1) // missing: becauseOf reason
                    }
                }
            }
        }
        it("two work days with same date") {
            val conflictingDate = TestConstants.someDate
            failingTimesheet {
                someWorkingDay(date = conflictingDate)
                someWorkingDay(date = conflictingDate)

            }.message shouldContain conflictingDate.year.toString()
                .substring(2) shouldContain conflictingDate.monthValue.toString() shouldContain conflictingDate.dayOfMonth.toString()
        }
        // TODO two day offs with same date
        // TODO 1 work day 1 day off; same date
        it("work entry without about") {
            failingTimesheet {
                someWorkingDay(someDate) {
                    anyTimeRangeString about " "
                }
            }.message shouldContain someDate.toParsableDate()
        }
        // TODO test 2 days off at same date
        it("two work entries with same time") {
            failingTimesheet {
                someWorkingDay(someDate) {
                    someWorkEntry(timeRange = someTimeRangeString)
                    someWorkEntry(timeRange = someTimeRangeString)
                }
            }.message shouldContain someDate.toParsableDate() // someTimeRangeString ... unfortunately this info is lost due to dynamic time construction and lack of validation info
        }
    }

    describe("When ... year-month-day") {
        it("When add work-day Then set date correctly") {
            timesheetAny {
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

            val sheet = timesheetAny {
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

    describe("When ... partial time range") {
        it("open end success") {
            val sheet = timesheetAny {
                someWorkingDay {
                    "0-" - "open end entry"
                    "1-2" - "last entry"
                }
            }

            sheet.entries.workEntries.size shouldBe 2
            sheet.entries.workEntries.first().dateRange.timeRange shouldBe (0 until 1)
        }
        it("open begin success") {
            val sheet = timesheetAny {
                someWorkingDay {
                    "0-1" - "first entry"
                    "-2" - "open begin entry"
                }
            }

            sheet.entries.workEntries.size shouldBe 2
            sheet.entries.workEntries.last().dateRange.timeRange shouldBe (1 until 2)
        }
        it("single open end fail") {
            val exception = failingTimesheet {
                someWorkingDay(LocalDate.of(2003, 2, 1)) {
                    "0-" - "open end entry"
                }
            }
            exception.message shouldContain "00:00-"
            exception.message shouldContain "1.2.03"
        }
        it("single end end fail") {
            val exception = failingTimesheet {
                someWorkingDay(LocalDate.of(2003, 2, 1)) {
                    "-1" - "open begin entry"
                }
            }
            exception.message shouldContain "1:00"
            exception.message shouldContain "1.2.03"
        }
        // TODO test overlaps
    }
    describe("days off") {
        it("range") {
            val sheet = timesheetAny {
                year(2000) {
                    month(JULY) {
                        someWorkingDay(1)
                        daysOff(2..3) becauseOf OffReason.any
                    }
                }
            }

            sheet.entries shouldHaveSize 3
            sheet.entries[1].shouldBeInstanceOf<DayOffEntry>()
            sheet.entries[2].shouldBeInstanceOf<DayOffEntry>()
            sheet.entries[1].day shouldBe LocalDate.of(2000, JULY, 2)
            sheet.entries[2].day shouldBe LocalDate.of(2000, JULY, 3)
        }
    }
    describe("day label") {
        it("incorrect fails") {
            shouldThrow<IllegalArgumentException> {
                timesheetAny {
                    year(2025) {
                        month(NOVEMBER) {
                            day(SATURDAY, 9) { // NO! it's sunday
                                "10-12" - "msg" - Tag.any
                            }
                        }
                    }
                }
            }
        }
        it("correct") {
            val sheet = timesheetAny {
                year(2025) {
                    month(NOVEMBER) {
                        day(SUNDAY, 9) {
                            "10-12" - "msg" - Tag.any
                        }
                    }
                }
            }
            sheet.entries.shouldBeSingleton()
        }
    }
    describe("contract") {
        it("skipping will get default") {
            val sheet = timesheetAny {
                someWorkingDay(someDate) {
                    someWorkEntry()
                }
            }
            sheet.contracts.shouldBeSingleton().first().contract shouldBe WorkContract.default
        }
        it("custom contract") {
            val sheet = timesheetAny {
                someWorkingDay(someDate) {
                    contract {
                        hoursPerWeek = 10
                        dayOff = friday
                    }
                    someWorkEntry()
                }
            }

            sheet.contracts.shouldBeSingleton().first().contract shouldBe WorkContract(
                daysOff = setOf(friday),
                hoursPerWeek = 10
            )
        }
        it("custom contract test dates") {
            val sheet = timesheetAny {
                year(2000) {
                    month(SEPTEMBER) {
                        day(1) {
                            contract {
                                hoursPerWeek = 10
                                daysOff = setOf(monday)
                            }
                            "9-10" - "x" - Tag.any
                        }
                    }
                }
            }
            sheet.contracts.shouldBeSingleton().first().dateRange shouldBe DateRange(
                startDate = LocalDate.parse("2000-09-01"),
                endDate = LocalDate.parse("2000-09-01")
            )
        }
    }
    /*
    year(2000) {
        month(september) {
            day(monday, 1st) {
                contract {
                    hoursPerWeek = 32
                    daysOff = friday
                }
                // TODO IDEA support different time definitions
                13 to 15.30 - "worked on slides" - orga
            }
            day(mon, 2st) {
                contract {
                    hoursPerWeek = 38
                    daysOff = none
                }
                14-15 - "foo" - meet
            }
        }
    }
     */
})
