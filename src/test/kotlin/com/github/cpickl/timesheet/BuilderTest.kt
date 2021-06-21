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
        // TODO build day without about
        "When build work day Then parse properly" {
            val date = LocalDate.of(2003, 2, 1)
            val timeStart = LocalTime.of(9, 30)
            val timeEnd = LocalTime.of(10, 0)
            val description = "intro meeting"

            val sheet = timesheet {
                day("${date.dayOfMonth}.${date.monthValue}.${date.year}") {
                    "9:30-10" about description tag (IntermediateTag.meet)
                }
            }

            sheet.startDate shouldBe date
            sheet.entries shouldBe TimeEntries(
                listOf(
                    WorkDayEntry(
                        hours = EntryDateRange(date, TimeRange(timeStart, timeEnd)),
                        description = description,
                        tag = Tag.Meeting,
                    )
                )
            )
        }
    }
}
