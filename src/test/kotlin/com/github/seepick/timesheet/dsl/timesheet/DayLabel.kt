package com.github.seepick.timesheet.dsl.timesheet

import com.github.seepick.timesheet.date.november
import com.github.seepick.timesheet.date.saturday
import com.github.seepick.timesheet.date.sunday
import com.github.seepick.timesheet.tags.Tag
import com.github.seepick.timesheet.tags.any
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import java.time.Month.NOVEMBER

class DayLabel : DescribeSpec({

    describe("day label") {
        it("incorrect fails") {
            shouldThrow<IllegalArgumentException> {
                timesheetAny("10.11.25") {
                    year(2025) {
                        november {
                            saturday(9) { // NO! it's sunday
                                "10-12" - "msg" - Tag.any
                            }
                        }
                    }
                }
            }
        }
        it("correct") {
            val sheet = timesheetAny("10.11.25") {
                year(2025) {
                    month(NOVEMBER) {
                        sunday(9) {
                            "10-12" - "msg" - Tag.any
                        }
                    }
                }
            }
            sheet.entries.shouldBeSingleton()
        }
    }
})
