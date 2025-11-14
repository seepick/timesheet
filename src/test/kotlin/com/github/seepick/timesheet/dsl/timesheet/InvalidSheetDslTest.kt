package com.github.seepick.timesheet.dsl.timesheet

import com.github.seepick.timesheet.date.toParsableDate
import com.github.seepick.timesheet.dsl.InvalidSheetException
import com.github.seepick.timesheet.test_infra.TestConstants
import com.github.seepick.timesheet.test_infra.TestConstants.someDate
import com.github.seepick.timesheet.test_infra.parseDate
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.next

class InvalidSheetDslTest : DescribeSpec({

    val anyDate = Arb.localDate().next()

//    val someTimeRange = TestConstants.someTimeRange
    val someTimeRangeString = TestConstants.someTimeRangeString
    val anyTimeRangeString = TestConstants.someTimeRangeString

    describe("Invalid setup") {
        it("When entry is in future Then fail") {
            shouldThrow<InvalidSheetException> {
                timesheetAny(someDate) {
                    someWorkingDay(someDate.plusDays(1)) {
                        someWorkEntry()
                    }
                }
            }.message.shouldNotBeNull().shouldContain("must not be in the future")
        }
        it("no entry") {
            shouldThrow<InvalidSheetException> {
                timesheetAny(someDate) {
                    someWorkingDay(someDate) {
                    }
                }
            }.message.shouldNotBeNull().shouldContain("at least one entry")
        }
    }
    // TODO test exception messages
    describe("When ... invalid Then fail") {
        it("no days") {
            failingTimesheet(anyDate) {}
        }
        it("starts with day-off day") {
            failingTimesheet(someDate) {
                someDayOff(someDate)
            }
        }
        it("Given some work day When day-off without reason entry") {
            val date = "13.11.25".parseDate() // thursday
            failingTimesheet(date) {
                year(date.year) {
                    month(date.month) {
                        someWorkingDay(12)
                        dayOff(13) // missing: becauseOf reason
                    }
                }
            }
        }
        it("two work days with same date") {
            val conflictingDate = someDate
            failingTimesheet(conflictingDate) {
                someWorkingDay(date = conflictingDate)
                someWorkingDay(date = conflictingDate)

            }.message shouldContain conflictingDate.year.toString()
                .substring(2) shouldContain conflictingDate.monthValue.toString() shouldContain conflictingDate.dayOfMonth.toString()
        }
        it("work entry without about") {
            failingTimesheet(someDate) {
                someWorkingDay(someDate) {
                    anyTimeRangeString about " "
                }
            }.message shouldContain someDate.toParsableDate()
        }
        it("two work entries with same time") {
            failingTimesheet(someDate) {
                someWorkingDay(someDate) {
                    someWorkEntry(timeRange = someTimeRangeString)
                    someWorkEntry(timeRange = someTimeRangeString)
                }
            }.message shouldContain someDate.toParsableDate() // someTimeRangeString ... unfortunately this info is lost due to dynamic time construction and lack of validation info
        }
    }
})
