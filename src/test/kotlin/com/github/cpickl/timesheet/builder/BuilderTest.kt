package com.github.cpickl.timesheet.builder

import com.github.cpickl.timesheet.DayOffEntry
import com.github.cpickl.timesheet.EntryDateRange
import com.github.cpickl.timesheet.OffTag
import com.github.cpickl.timesheet.Tag
import com.github.cpickl.timesheet.TestConstants
import com.github.cpickl.timesheet.TimeEntries
import com.github.cpickl.timesheet.TimeRange
import com.github.cpickl.timesheet.WorkDayEntry
import com.github.cpickl.timesheet.someDayOff
import com.github.cpickl.timesheet.someTime
import com.github.cpickl.timesheet.someWorkingDate
import com.github.cpickl.timesheet.someWorkingDay
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalTime

class BuilderTest : DescribeSpec() {

    private val someDate = TestConstants.anyDate
    private val date1 = TestConstants.anyDate
    private val date2 = date1.plusDays(1)
    private val description = "test description"

    init {
        describe("When ... valid Then return") {
            it("Given some working day When build with some date Then sheet's start date is set accordingly") {
                val sheet = timesheet {
                    someWorkingDate(someDate)
                }

                sheet.startDate shouldBe someDate
            }
            it("work day") {
                val timeStart = LocalTime.of(9, 30)
                val timeEnd = LocalTime.of(10, 0)

                val sheet = timesheet {
                    day(someDate.toParsableDate()) {
                        "9:30-10" about description tag (TagDso.meet)
                    }
                }

                sheet.entries shouldBe TimeEntries(
                    listOf(
                        WorkDayEntry(
                            hours = EntryDateRange(someDate, TimeRange(timeStart, timeEnd)),
                            about = description,
                            tag = Tag.Meeting,
                        )
                    )
                )
            }
            it("day off") {
                val sheet = timesheet {
                    someWorkingDate(date1)
                    dayOff(date2.toParsableDate()) becauseOf DayOffReasonDso.PublicHoliday
                }

                sheet.entries shouldContain DayOffEntry(
                    day = date2,
                    tag = OffTag.PublicHoliday,
                )
            }
        }
        describe("When ... invalid Then fail") {
            it("no entries") {
                shouldThrow<BuilderException> {
                    timesheet {}
                }
            }
            it("starts with non-workday entry") {
                shouldThrow<BuilderException> {
                    timesheet {
                        someDayOff()
                    }
                }
            }
            it("day-off without reason entry") {
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
        }
    }
}

fun LocalDate.toParsableDate() = "$dayOfMonth.$monthValue.${year.toString().substring(2)}"
