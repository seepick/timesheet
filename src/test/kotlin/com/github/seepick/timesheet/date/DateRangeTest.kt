package com.github.seepick.timesheet.date

import com.github.seepick.timesheet.calc.invoke
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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
        describe("limitedBy") {
            it("within should limit both ends") {
                DateRange("1.1.00" to "10.1.00").limitedBy(DateRange("5.1.00" to "6.1.00")) shouldBe DateRange("5.1.00" to "6.1.00")
            }
            it("outside should not limit") {
                DateRange("5.1.00" to "6.1.00").limitedBy(DateRange("1.1.00" to "10.1.00")) shouldBe DateRange("5.1.00" to "6.1.00")
            }
            it("same should not change") {
                DateRange("5.1.00" to "6.1.00").limitedBy(DateRange("5.1.00" to "6.1.00")) shouldBe DateRange("5.1.00" to "6.1.00")
            }
        }
        describe("limitedEndBy") {
            it("does limit") {
                DateRange("1.1.00" to "5.1.00").limitedEndBy("3.1.00".parseDate()) shouldBe DateRange("1.1.00" to "3.1.00")
            }
            it("does not limit") {
                DateRange("1.1.00" to "5.1.00").limitedEndBy("10.1.00".parseDate()) shouldBe DateRange("1.1.00" to "5.1.00")
            }
        }

    }
}
