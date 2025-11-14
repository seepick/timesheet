package com.github.seepick.timesheet.dsl

import com.github.seepick.timesheet.date.ClosedRangeSpec
import com.github.seepick.timesheet.date.OpenEndRangeSpec
import com.github.seepick.timesheet.date.TimeRange
import com.github.seepick.timesheet.date.closedRangeSpec
import com.github.seepick.timesheet.date.hrs
import com.github.seepick.timesheet.off.DayOffEntry
import com.github.seepick.timesheet.off.offReason
import com.github.seepick.timesheet.timesheet.EntryDateRange
import com.github.seepick.timesheet.timesheet.WorkDayEntry
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class BuilderTransformerKtTest : DescribeSpec() {
    init {
        describe("work day") {
            it("closed time - sunshine") {
                val closedTime = Arb.closedRangeSpec().next()
                val workEntry = Arb.builderWorkDayEntry().next()
                    .copy(timeRangeSpec = closedTime)

                val result = workEntry.toRealEntry(neighbours = null to null)

                result.shouldBeSingleton().first().shouldBe(
                    WorkDayEntry(
                        dateRange = EntryDateRange(
                            day = workEntry.day,
                            timeRange = TimeRange(start = closedTime.start, end = closedTime.end)
                        ),
                        about = workEntry.about,
                        tags = workEntry.tags,
                    )
                )
            }
            it("open closed time range") {
                val workEntry = Arb.builderWorkDayEntry().next()
                    .copy(timeRangeSpec = OpenEndRangeSpec("8:00".hrs))
                val workNeighbour = Arb.builderWorkDayEntry().next()
                    .copy(timeRangeSpec = ClosedRangeSpec("9:00".hrs, "10:00".hrs))

                val result = workEntry.toRealEntry(null to workNeighbour)

                result[0].shouldBeInstanceOf<WorkDayEntry>().timeRange shouldBe TimeRange("8:00".hrs, "9:00".hrs)
            }
        }

        describe("day off") {
            it("sunshine") {
                val reason = Arb.offReason().next()
                val dayOff = Arb.builderDayOffEntry().next().copy(reason = reason)

                val result = dayOff.toRealEntry(neighbours = null to null)

                result.shouldBeSingleton().first().shouldBe(
                    DayOffEntry(
                        day = dayOff.day,
                        reason = reason,
                    )
                )
            }
        }
    }
}
