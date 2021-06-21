package com.github.cpickl.timesheet

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TimeSheetTest : StringSpec() {
    init {
        "start date properly calculated" {
            val sheet = timesheet {
                fullDay("1.1.21")
                someDayOff("2.1.21")
            }

            sheet.startDate shouldBe "1.1.21".parseDate()
        }
    }
}
