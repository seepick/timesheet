package com.github.cpickl.timesheet

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalTime

class BuilderTest : StringSpec() {
    init {
        "When timesheet without entries Then fail" {
            shouldThrow<InvalidTimesheetModelException> {
                timesheet {}
            }
        }
        "When timesheet starts with non workday entry Then fail" {
            shouldThrow<InvalidTimesheetModelException> {
                timesheet {
                    someDayOff()
                }
            }
        }
        "When day off without reason entry Then fail" {
            shouldThrow<InvalidTimesheetModelException> {
                timesheet {
                    someWorkingDay()
                    dayOff("1.1.00")
                }
            }
        }
        "When build work day Then parse properly" {
            val sheet = timesheet {
                day("1.2.03") {
                    "9-10" about "intro meeting" tag (IntermediateTag.Meet)
                }
            }

            sheet.startDate shouldBe LocalDate.of(2003, 2, 1)
            sheet.entries shouldBe TimeEntries(
                listOf(
                    WorkDayEntry(
                        hours = EntryDateRange(
                            LocalDate.of(2003, 2, 1),
                            TimeRange(LocalTime.of(9, 0), LocalTime.of(10, 0))
                        ),
                        description = "intro meeting",
                        tag = Tag.Meeting,
                    )
                )
            )
        }
    }
}

private fun TimeSheetDsl.someDayOff() {
    dayOff("1.1.00") becauseOf DayOffReason.any
}

private fun TimeSheetDsl.someWorkingDay() {
    day("1.2.03") {
        "9-10" about "some" tag (IntermediateTag.any)
    }
}
