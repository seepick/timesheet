package com.github.seepick.timesheet.dsl.timesheet

import com.github.seepick.timesheet.dsl.InvalidSheetException
import com.github.seepick.timesheet.off.DayOffEntry
import com.github.seepick.timesheet.off.OffReason
import com.github.seepick.timesheet.off.any
import com.github.seepick.timesheet.test_infra.TestConstants.someDate
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.LocalDate
import java.time.Month.JULY

class DayOffDslTest : DescribeSpec({

    val someOffReason = OffReason.any

    describe("single day off") {
        it("happy path") {
            val sheet = timesheetAny(today = someDate) {
                someWorkingDay(someDate.minusDays(1))
                dayOff(someDate, someOffReason)
            }

            sheet.entries.size shouldBe 2
            sheet.entries shouldContain DayOffEntry(
                day = someDate,
                reason = someOffReason,
            )
        }
        it("work day and day off at same date") {
            shouldThrow<InvalidSheetException> {
                timesheetAny(today = someDate) {
                    someWorkingDay(someDate)
                    dayOff(someDate, someOffReason)
                }
            }
        }
        it("first day is day off fails") {
            shouldThrow<InvalidSheetException> {
                timesheetAny(today = someDate) {
                    dayOff(someDate, someOffReason)
                }
            }.cause.shouldNotBeNull().message shouldContain "First entry must be a working day"
        }
        it("two day off at same date") {
            shouldThrow<InvalidSheetException> {
                timesheetAny(today = someDate) {
                    someWorkingDay(someDate.minusDays(1))
                    dayOff(someDate, someOffReason)
                    dayOff(someDate, someOffReason)
                }
            }
        }

    }
    describe("multiple days off") {
        it("range") {
            val sheet = timesheetAny(today = "3.7.00") {
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
})
