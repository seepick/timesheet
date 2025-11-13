package com.github.seepick.timesheet.date

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import java.time.LocalDate

class DateRangeTest : DescribeSpec() {

    private val date = LocalDate.of(2000, 1, 1)

    init {
        describe("iterator") {
            it("same date yields single entry") {
                DateRange(
                    startDate = date,
                    endDate = date,
                ).toList() shouldHaveSize 1
            }
        }
    }
}
