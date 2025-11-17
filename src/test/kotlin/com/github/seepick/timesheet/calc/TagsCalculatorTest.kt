package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.ReportView
import com.github.seepick.timesheet.date.TimeRange
import com.github.seepick.timesheet.date.parseDate
import com.github.seepick.timesheet.dsl.TimeSheetDsl
import com.github.seepick.timesheet.dsl.timesheet.someWorkEntry2
import com.github.seepick.timesheet.dsl.timesheet.someWorkingDay
import com.github.seepick.timesheet.dsl.timesheet.timesheetAny
import com.github.seepick.timesheet.tags.tag1
import com.github.seepick.timesheet.test_infra.TestConstants
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime

@OptIn(ExperimentalKotest::class)
class TagsCalculatorTest : DescribeSpec({
    val today = "3.1.00".parseDate()
    val dateRange = DateRange("1.1.00" to "30.1.00")
    val someTag = TestConstants.tag1
    val timeRange = TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0))

    fun calc(
        reportView: ReportView = ReportView.TotalReportView(dateRange),
        sheet: TimeSheetDsl.() -> Unit,
    ) = calculateTags(reportView, timesheetAny(today = today, entryCode = sheet))

    describe("fo").config(enabled = false) {
        it("ba") {

            val report = calc {
                someWorkingDay(date = today) {
                    someWorkEntry2(timeRange, tags = listOf(someTag))
                }
            }
            report.minutesPerTag shouldBe mapOf(someTag to timeRange.duration)
        }
    }
    // TODO more tags tests
})
