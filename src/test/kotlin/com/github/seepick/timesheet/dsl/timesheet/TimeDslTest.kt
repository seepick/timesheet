package com.github.seepick.timesheet.dsl.timesheet

import com.github.seepick.timesheet.date.until
import com.github.seepick.timesheet.test_infra.parseDate
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class TimeDslTest : DescribeSpec({

    val someDateString = "13.11.25" // thursday
    val someDate = someDateString.parseDate()

    describe("When ... partial time range") {
        it("open end success") {
            val sheet = timesheetAny(someDate) {
                someWorkingDay(someDate) {
                    "0-" - "open end entry"
                    "1-2" - "last entry"
                }
            }

            sheet.entries.workEntries.size shouldBe 2
            sheet.entries.workEntries.first().dateRange.timeRange shouldBe (0 until 1)
        }
        it("open begin success") {
            val sheet = timesheetAny(someDate) {
                someWorkingDay(someDate) {
                    "0-1" - "first entry"
                    "-2" - "open begin entry"
                }
            }

            sheet.entries.workEntries.size shouldBe 2
            sheet.entries.workEntries.last().dateRange.timeRange shouldBe (1 until 2)
        }
        it("single open end fail") {
            val exception = failingTimesheet(someDate) {
                someWorkingDay(someDate) {
                    "0-" - "open end entry"
                }
            }
            exception.message shouldContain "00:00-"
            exception.message shouldContain someDateString
        }
        it("single end end fail") {
            val exception = failingTimesheet(someDate) {
                someWorkingDay(someDate) {
                    "-1" - "open begin entry"
                }
            }
            exception.message shouldContain "1:00"
            exception.message shouldContain someDateString
        }
    }
})
