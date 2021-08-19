package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.TimeSheetDsl
import com.github.cpickl.timesheet.builder.TimeSheetInitDsl
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Month

class TimeCalculatorTest : StringSpec() {

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

        "filter free day" {
            val report = calculate("2.6.21", { daysOff += WorkDay.Wednesday }) {
                year(2021) {
                    month(Month.JUNE) {
                        day(1) {
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

    private fun calculate(today: String, initCode: TimeSheetInitDsl.() -> Unit = {}, sheet: TimeSheetDsl.() -> Unit) =
        TimeCalculator(clockReturning(today))
            .calculate(timesheet(init = initCode, entryCode = sheet))

    private fun clockReturning(date: String): Clock {
        val clock = mockk<Clock>()
        every { clock.currentLocalDate() } returns date.parseDate()
        return clock
    }
}

