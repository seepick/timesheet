package com.github.cpickl.timesheet.builder

import com.github.cpickl.timesheet.EntryDateRange
import com.github.cpickl.timesheet.Tag
import com.github.cpickl.timesheet.TimeEntries
import com.github.cpickl.timesheet.TimeRange
import com.github.cpickl.timesheet.WorkDayEntry
import com.github.cpickl.timesheet.someDayOff
import com.github.cpickl.timesheet.someTime
import com.github.cpickl.timesheet.someWorkingDay
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalTime

class BuilderTest : DescribeSpec() {
    init {
        describe("Misc") {
            it("When timesheet without entries Then fail") {
                shouldThrow<BuilderException> {
                    timesheet {}
                }
            }
            it("When timesheet starts with non workday entry Then fail") {
                shouldThrow<BuilderException> {
                    timesheet {
                        someDayOff()
                    }
                }
            }
            it("When day off without reason entry Then fail") {
                shouldThrow<BuilderException> {
                    timesheet {
                        someWorkingDay()
                        dayOff("1.1.00")
                    }
                }
            }
            it("When build two entries with same date Then fail") {
                shouldThrow<BuilderException> {
                    timesheet {
                        val conflictingDate = "1.1.21"
                        someWorkingDay(date = conflictingDate)
                        someWorkingDay(date = conflictingDate)
                    }
                }
            }
            it("When build entry without about message Then fail") {
                shouldThrow<BuilderException> {
                    timesheet {
                        someWorkingDay {
                            someTime about " "
                        }
                    }
                }
            }
            it("When build work day Then parse properly") {
                val date = LocalDate.of(2003, 2, 1)
                val timeStart = LocalTime.of(9, 30)
                val timeEnd = LocalTime.of(10, 0)
                val description = "intro meeting"

                val sheet = timesheet {
                    day("1.2.03") {
                        "9:30-10" about description tag (TagDso.meet)
                    }
                }

                sheet.startDate shouldBe date
                sheet.entries shouldBe TimeEntries(
                    listOf(
                        WorkDayEntry(
                            hours = EntryDateRange(date, TimeRange(timeStart, timeEnd)),
                            about = description,
                            tag = Tag.Meeting,
                        )
                    )
                )
            }
        }
    }
}

fun LocalDate.toParsableDate() = "$dayOfMonth.$monthValue.${year.toString().substring(2)}"
