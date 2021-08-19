package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.Tags
import com.github.cpickl.timesheet.builder.timesheet
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Month

class TimeSheetTest : StringSpec() {
    init {
        "start date properly calculated" {
            val sheet = timesheet {
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
