package com.github.seepick.timesheet

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Month

class TimeSheetTest : StringSpec() {
    init {
        "start date properly calculated" {
            val sheet = timesheetAny {
                year(2021) {
                    month(Month.JANUARY) {
                        day(1) {
                            "10-18" - "any"
                        }
                        dayOff(2) becauseOf OffReason.any
                    }
                }
            }

            sheet.startDate shouldBe "1.1.21".parseDate()
        }
    }
}
