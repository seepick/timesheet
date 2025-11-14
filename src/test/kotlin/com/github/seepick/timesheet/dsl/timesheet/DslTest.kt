package com.github.seepick.timesheet.dsl.timesheet

import com.github.seepick.timesheet.date.timeRange
import com.github.seepick.timesheet.tags.Tag
import com.github.seepick.timesheet.tags.any
import com.github.seepick.timesheet.test_infra.TestConstants
import com.github.seepick.timesheet.test_infra.parseDate
import com.github.seepick.timesheet.timesheet.EntryDateRange
import com.github.seepick.timesheet.timesheet.TimeEntries
import com.github.seepick.timesheet.timesheet.WorkDayEntry
import com.github.seepick.timesheet.timesheet.byTimeEntries
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class DslTest : DescribeSpec({

    val someDateString = "13.11.25" // thursday
    val someDate = someDateString.parseDate()
    val someTimeRange = Arb.timeRange().next()
    val timeRange1 = TestConstants.timeRange1
    val timeRange2 = TestConstants.timeRange2
    val description = "test description"
    val someTag = Tag.any

    describe("When sunshine case") {
        it("two valid working days Then two entries returned") {
            val sheet = timesheetAny(someDate) {
                someWorkingDay(someDate) {
                    someWorkEntry(timeRange = timeRange1.toParseableString())
                    someWorkEntry(timeRange = timeRange2.toParseableString())
                }
            }
            sheet.entries.size shouldBe 2
        }
        it("valid work day Then parsed entry returned") {
            val sheet = timesheetAny(someDate) {
                someWorkingDay(someDate) {
                    someTimeRange.formatted about description tag (someTag)
                }
            }

            sheet.entries shouldBe TimeEntries.byTimeEntries(
                listOf(
                    WorkDayEntry(
                        dateRange = EntryDateRange(someDate, someTimeRange),
                        about = description,
                        tags = setOf(someTag),
                    )
                )
            )
        }
    }
})
