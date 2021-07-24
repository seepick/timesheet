package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.Tags
import com.github.cpickl.timesheet.builder.timesheet
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TimeSheetTest : StringSpec() {
    init {
        "start date properly calculated" {
            val sheet = timesheet {
                fullWorkingDay("1.1.21")
                someDayOff("2.1.21")
            }

            sheet.startDate shouldBe "1.1.21".parseDate()
        }
    }
}
