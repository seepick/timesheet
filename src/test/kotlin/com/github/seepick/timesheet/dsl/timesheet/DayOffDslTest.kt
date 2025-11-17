package com.github.seepick.timesheet.dsl.timesheet

import com.github.seepick.timesheet.off.DayOffEntry
import com.github.seepick.timesheet.off.OffReason
import com.github.seepick.timesheet.off.any
import com.github.seepick.timesheet.test_infra.TestConstants.someDate
import com.github.seepick.timesheet.test_infra.parseDate
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.LocalDate
import java.time.Month.JULY

class DayOffDslTest : DescribeSpec({

    val someOffReason = OffReason.any

    describe("days off") {
        it("range") {
            val sheet = timesheetAny("3.7.00") {
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
    describe("fo") {
        it("day off") {
            val sheet = timesheetAny(someDate) {
                someWorkingDay(someDate.minusDays(1))
                dayOff(someDate, someOffReason)
            }

            sheet.entries shouldContain DayOffEntry(
                day = someDate,
                reason = someOffReason,
            )
        }

        it("Given work-day When add day-off Then set date correctly") {
            val day1 = 12
            val day2 = 13
            val date = "13.11.25".parseDate() // thursday

            val sheet = timesheetAny(date) {
                year(date.year) {
                    month(date.month) {
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

    // TODO more tests for days off

    // test: days off with same date
    // test: 1 work day 1 day off; same date
    // test: 2 days off at same date


})
