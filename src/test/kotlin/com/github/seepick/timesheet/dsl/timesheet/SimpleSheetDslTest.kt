package com.github.seepick.timesheet.dsl.timesheet

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class SimpleSheetDslTest : DescribeSpec({

    val someDate = LocalDate.of(2025, 11, 10) // monday


    describe("When ... year-month-day") {
        it("When add work-day Then set date of first entry correctly") {
            timesheetAny(someDate) {
                year(someDate.year) {
                    month(someDate.month) {
                        day(someDate.dayOfMonth) {
                            someWorkEntry()
                        }
                    }
                }
            } shouldHaveSingleEntryWithDate someDate
        }
    }

    describe("When check sheet start date") {
        it("Given single entry Then start date equals entry date") {
            val sheet = timesheetAny(someDate) {
                someWorkingDay(someDate) {
                    someWorkEntry()
                }
            }

            sheet.startDate shouldBe someDate
        }
    }
    describe("Edge cases") {
        it("Given day with entry When other day no entry Then succeed") {
            timesheetAny(someDate.plusWeeks(1)) {
                someWorkingDay(someDate) {
                    someWorkEntry()
                }
                someWorkingDay(someDate.plusDays(1)) {
                }
            }
        }
    }
})
