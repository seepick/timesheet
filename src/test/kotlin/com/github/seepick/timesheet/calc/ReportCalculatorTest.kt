package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.Clock
import com.github.seepick.timesheet.date.WorkDay.wednesday
import com.github.seepick.timesheet.date.june
import com.github.seepick.timesheet.date.tuesday
import com.github.seepick.timesheet.dsl.TimeSheetDsl
import com.github.seepick.timesheet.dsl.timesheet.someDayOff
import com.github.seepick.timesheet.test_infra.parseDate
import com.github.seepick.timesheet.test_infra.timesheetAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Month

class ReportCalculatorTest : StringSpec() {

    private val anyDay = 1
    private val minutesInHour = 60L
    private val workHoursPerDay = 8L
    private val workMinutesPerDay = workHoursPerDay * minutesInHour

    init {
        "single complete work day" {
            val report = calculate("1.6.21") {
                year(2021) {
                    month(Month.JUNE) {
                        day(1) {
                            "10-18" about "any"
                        }
                    }
                }
            }

            report.totalMinutesToWork shouldBe 8 * minutesInHour
            report.totalMinutesWorked shouldBe 8 * minutesInHour
            report.balanceInMinutes shouldBe 0
        }

        "single incomplete work day" {
            val report = calculate(today = "1.2.21") {
                year(2021) {
                    month(Month.FEBRUARY) {
                        day(1) {
                            "10-16" about "any"
                        }
                    }
                }
            }

            report.totalMinutesToWork shouldBe 8 * minutesInHour
            report.totalMinutesWorked shouldBe 6 * minutesInHour
            report.balanceInMinutes shouldBe -2 * minutesInHour
        }

        "filter weekend" {
            val report = calculate("5.6.21") {
                year(2021) {
                    month(Month.JUNE) {
                        day(4) {
                            "10-18" - "any"
                        }
                    }
                }
            }

            report.totalMinutesToWork shouldBe workMinutesPerDay
        }

        "Given wednesday off and work on tuesday When report until wednesday Then filter out free day" {
            val report = calculate("2.6.21") { // next day is wednesday, which is off
                year(2021) {
                    june {
                        tuesday(1) {
                            contract {
                                hoursPerWeek = 4
                                dayOff = wednesday
                            }
                            "10-18" - "any"
                        }
                    }
                }
            }

            report.totalMinutesToWork shouldBe workMinutesPerDay
        }

        "filter day off" {
            val report = calculate("2.6.21") {
                year(2021) {
                    month(Month.JUNE) {
                        day(1) {
                            "10-18" - "any"
                        }
                        someDayOff(2)
                    }
                }
            }

            report.totalMinutesToWork shouldBe (8 * minutesInHour)
            report.totalMinutesWorked shouldBe (8 * minutesInHour)
            report.balanceInMinutes shouldBe 0
        }
    }

    private fun calculate(today: String, sheet: TimeSheetDsl.() -> Unit) =
        ReportCalculator(clockReturning(today))
            .calculate(timesheetAny(entryCode = sheet))

    private fun clockReturning(date: String): Clock {
        val clock = mockk<Clock>()
        every { clock.currentLocalDate() } returns date.parseDate()
        return clock
    }
}

