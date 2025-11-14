package com.github.seepick.timesheet.dsl.timesheet

import com.github.seepick.timesheet.contract.WorkContract
import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.WorkDay.friday
import com.github.seepick.timesheet.date.WorkDay.monday
import com.github.seepick.timesheet.tags.Tag
import com.github.seepick.timesheet.tags.any
import com.github.seepick.timesheet.test_infra.TestConstants.someDate
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.Month.SEPTEMBER

class ContractDslTest : DescribeSpec({

    describe("contract") {
        it("skipping will get default") {
            val sheet = timesheetAny(someDate) {
                someWorkingDay(someDate) {
                    someWorkEntry()
                }
            }
            sheet.contracts.shouldBeSingleton().first().contract shouldBe WorkContract.default
        }
        it("custom contract") {
            val sheet = timesheetAny(someDate) {
                someWorkingDay(someDate) {
                    contract {
                        hoursPerWeek = 10
                        dayOff = friday
                    }
                    someWorkEntry()
                }
            }

            sheet.contracts.shouldBeSingleton().first().contract shouldBe WorkContract(
                daysOff = setOf(friday),
                hoursPerWeek = 10
            )
        }
        it("custom contract test dates") {
            val sheet = timesheetAny("1.9.00") {
                year(2000) {
                    month(SEPTEMBER) {
                        day(1) {
                            contract {
                                hoursPerWeek = 10
                                daysOff = setOf(monday)
                            }
                            "9-10" - "x" - Tag.any
                        }
                    }
                }
            }
            sheet.contracts.shouldBeSingleton().first().dateRange shouldBe DateRange(
                startDate = LocalDate.parse("2000-09-01"),
                endDate = LocalDate.parse("2000-09-01")
            )
        }
    }
})
