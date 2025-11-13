package com.github.seepick.timesheet.timesheet

import com.github.seepick.timesheet.date.monday
import com.github.seepick.timesheet.date.november
import com.github.seepick.timesheet.date.th
import com.github.seepick.timesheet.dsl.timesheet.someWorkEntry
import com.github.seepick.timesheet.test_infra.timesheetAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class TimeSheetTest : StringSpec() {
    init {
        "start date properly calculated" {
            val sheet = timesheetAny {
                year(2025) {
                    november {
                        monday(10.th) {
                            someWorkEntry()
                        }
                    }
                }
            }

            sheet.startDate shouldBe LocalDate.parse("2025-11-10")
        }
    }
}
