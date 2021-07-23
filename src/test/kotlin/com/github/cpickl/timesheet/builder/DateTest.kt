package com.github.cpickl.timesheet.builder

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class DateTest : DescribeSpec() {
    init {
        describe("LocalDate.toParseableString") {
            it("sunshine") {
                val date = LocalDate.of(2003, 2, 1)

                date.toParsableDate() shouldBe "1.2.03"
            }
        }
    }
}