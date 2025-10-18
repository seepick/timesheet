package com.github.seepick.timesheet.builder

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
            row("8:00-9:00", TimeRangeSpec.ClosedRangeSpec(8.h, 9.h)),
            row("8-9", TimeRangeSpec.ClosedRangeSpec(8.h, 9.h)),
            row("8-9:30", TimeRangeSpec.ClosedRangeSpec(8.h, 9 h 30)),
            row("8:30-9", TimeRangeSpec.ClosedRangeSpec(8 h 30, 9.h)),
        ) { input, expectedRange ->
            input.asClue {
                TimeRangeSpec.parse(input) shouldBe expectedRange
            }
        }
    }
    describe("partial") {
        forAll(
            row("8-", TimeRangeSpec.OpenEndRangeSpec(start = 8.h)),
            row("8:30-", TimeRangeSpec.OpenEndRangeSpec(start = 8 h 30)),
            row("-8", TimeRangeSpec.OpenStartRangeSpec(end = 8.h)),
            row("-8:30", TimeRangeSpec.OpenStartRangeSpec(end = 8 h 30)),
        ) { input, expectedRange ->
            input.asClue {
                TimeRangeSpec.parse(input) shouldBe expectedRange
            }
        }
    }
    describe("invalid") {
        forAll(
            row("-", "-"),
            row("8:-9", "8:"),
            row("8:-", "8:"),
            row("8:0-9", "8:0"),
            row("8:0-", "8:0"),
            row("8-9:0", "9:0"),
            row("-9:0", "9:0"),
            row("8:60-9", "60"),
            row("24-9", "24"),
            row("9-8", "9"),
        ) { input, errorMessage ->
            input.asClue {
                shouldThrow<TimeParseException> {
                    TimeRangeSpec.parse(input)
                }.message shouldContain errorMessage
            }
        }
    }
})

private val Int.h: LocalTime get() = LocalTime.of(this, 0)
private infix fun Int.h(m: Int) = LocalTime.of(this, m)
