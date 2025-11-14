package com.github.seepick.timesheet.date

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime

class TimeRangeTest : DescribeSpec({
    val time1 = LocalTime.of(1, 0)
    val time2 = LocalTime.of(2, 0)
    val time3 = LocalTime.of(3, 0)
    val time4 = LocalTime.of(4, 0)

    describe("start before-equals end") {
        it("start before end - success") {
            TimeRange(time1, time2)
        }
        it("start after end - fail") {
            shouldThrow<IllegalArgumentException> {
                TimeRange(time2, time1)
            }
        }
        it("both same - fail") {
            shouldThrow<IllegalArgumentException> {
                TimeRange(time1, time1)
            }
        }
    }
    describe("not overlaps") {
        it("range A before range B") {
            TimeRange(time1, time2).overlaps(TimeRange(time3, time4)) shouldBe false
        }
        it("range A after range B") {
            TimeRange(time3, time4).overlaps(TimeRange(time1, time2)) shouldBe false
        }
    }
    describe("overlaps") {
        it("range A equals range B") {
            TimeRange(time1, time2).overlaps(TimeRange(time1, time2)) shouldBe true
        }
        it("range A before range B") {
            TimeRange(time1, time3).overlaps(TimeRange(time2, time4)) shouldBe true
        }
        it("range A after range B") {
            TimeRange(time2, time4).overlaps(TimeRange(time1, time3)) shouldBe true
        }
        it("range A bigger range B") {
            TimeRange(time1, time4).overlaps(TimeRange(time2, time3)) shouldBe true
        }
        it("range A smaller range B") {
            TimeRange(time2, time3).overlaps(TimeRange(time1, time4)) shouldBe true
        }
    }
})
