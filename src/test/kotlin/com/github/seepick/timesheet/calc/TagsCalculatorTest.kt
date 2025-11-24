package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.ReportView
import com.github.seepick.timesheet.date.TimeRange
import com.github.seepick.timesheet.date.parseDate
import com.github.seepick.timesheet.dsl.TimeSheetDsl
import com.github.seepick.timesheet.dsl.timesheet.someWorkEntryBySpec
import com.github.seepick.timesheet.dsl.timesheet.someWorkingDay
import com.github.seepick.timesheet.dsl.timesheet.timesheetAny
import com.github.seepick.timesheet.report.TagsReport
import com.github.seepick.timesheet.tags.tag
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import java.time.LocalTime

class TagsCalculatorTest : DescribeSpec({
    val today = "3.1.00".parseDate()
    val dateRange = DateRange("1.1.00" to "30.1.00")
    val someTag = Arb.tag().next()
    val timeRange = TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0))

    fun calc(
        reportView: ReportView = ReportView.TotalReportView(dateRange),
        sheet: TimeSheetDsl.() -> Unit,
    ) = calculateTags(reportView, timesheetAny(today = today, entryCode = sheet))

    describe("simple sunshine cases") {
        it("given single entry with tag then duration returned") {
            val report = calc {
                someWorkingDay(date = today) {
                    someWorkEntryBySpec(timeRange, tags = listOf(someTag))
                }
            }
            report.minutesPerTag shouldBe mapOf(someTag to timeRange.durationInMin)
        }
    }
    // TODO more tags tests
})

class TagsReportTest : DescribeSpec({
    val someTag = Arb.tag().next()
    val tag1 = Arb.tag().next()
    val tag2 = Arb.tag().next()

    describe("percentagesPerTag") {
        it("if empty") {
            TagsReport(emptyMap()).percentagesPerTag
                .shouldBeEmpty()
        }
        it("if single then it has 100%") {
            TagsReport(mapOf(someTag to 1)).percentagesPerTag shouldBeEqual
                    mapOf(someTag to 1.0)
        }

        it("if two then split") {
            TagsReport(mapOf(tag1 to 3, tag2 to 1)).percentagesPerTag shouldBeEqual
                    mapOf(tag1 to 0.75, tag2 to 0.25)
        }
    }
})
