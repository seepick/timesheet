package com.github.cpickl.timesheet

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalTime

class BuilderTest : StringSpec() {
    init {
        "When build work day Then parse properly" {
            val sheet = timesheet {
                day("1.6.21") {
                    "9-10" about "intro meeting" tag (IntermediateTag.Meet)
                }
            }

            sheet.startDate shouldBe LocalDate.of(2021, 6, 1)
            sheet.entries shouldBe TimeEntries(
                listOf(
                    WorkTimeEntry(
                        hours = EntryDateRange(
                            LocalDate.of(2021, 6, 1),
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
