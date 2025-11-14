package com.github.seepick.timesheet.date

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.LocalTime

class TimeRangeSpecTest : DescribeSpec({
    describe("simple") {
        forAll<String, TimeRangeSpec>(
            // single digit
            row("8:00-9:00", ClosedRangeSpec(8.h, 9.h)),
            row("8-9", ClosedRangeSpec(8.h, 9.h)),
            row("8-9:30", ClosedRangeSpec(8.h, 9 h 30)),
            row("8:30-9", ClosedRangeSpec(8 h 30, 9.h)),
            // double digit
            row("18:00-19:00", ClosedRangeSpec(18.h, 19.h)),
            row("18-19", ClosedRangeSpec(18.h, 19.h)),
            row("18-19:30", ClosedRangeSpec(18.h, 19 h 30)),
            row("18:30-19", ClosedRangeSpec(18 h 30, 19.h)),
        ) { input, expectedRange ->
            input.asClue {
                TimeRangeSpec.parse(input) shouldBe expectedRange
            }
        }
    }
    describe("partial") {
        forAll(
            // single digit
            row("8-", OpenEndRangeSpec(start = 8.h)),
            row("8:30-", OpenEndRangeSpec(start = 8 h 30)),
            row("-8", OpenStartRangeSpec(end = 8.h)),
            row("-8:30", OpenStartRangeSpec(end = 8 h 30)),
            // double digit
            row("18-", OpenEndRangeSpec(start = 18.h)),
            row("18:30-", OpenEndRangeSpec(start = 18 h 30)),
            row("-18", OpenStartRangeSpec(end = 18.h)),
            row("-18:30", OpenStartRangeSpec(end = 18 h 30)),
        ) { input, expectedRange ->
            input.asClue {
                TimeRangeSpec.parse(input) shouldBe expectedRange
            }
        }
    }
    describe("invalid") {
        forAll(
            row("", ""),
            row("8", "8"),
            row("8:-9", "8:"),
            row("-", "-"),
            row("8:-9", "8:"),
            row("8:-", "8:"),
            row("8:0-9", "8:0"),
            row("8:1-9", "8:1"),
            row("8:0-", "8:0"),
            row("8-9:0", "9:0"),
            row("-9:0", "9:0"),
            row("8:60-9", "60"),
            row("24-9", "24"),
            row("9-8", "9"),
        ) { input, errorMessage ->
            input.asClue {
                shouldThrow<TimeRangeSpecParseException> {
                    TimeRangeSpec.parse(input)
                }.message shouldContain errorMessage
            }
        }
    }
})

private val Int.h: LocalTime get() = LocalTime.of(this, 0)
private infix fun Int.h(m: Int) = LocalTime.of(this, m)
