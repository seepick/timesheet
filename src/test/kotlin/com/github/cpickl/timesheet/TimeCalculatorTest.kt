package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.TimeSheetDsl
import com.github.cpickl.timesheet.builder.TimeSheetInitDsl
import com.github.cpickl.timesheet.builder.timesheet
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class TimeCalculatorTest : StringSpec() {

    private val minutesInHour = 60L
    private val workHoursPerDay = 8L
    private val workMinutesPerDay = workHoursPerDay * minutesInHour

    init {
        "single complete work day" {
            val report = calculate("1.6.21") {
                fullWorkingDay("1.6.21")
            }

            report.totalMinutesToWork shouldBe 8 * minutesInHour
            report.totalMinutesWorked shouldBe 8 * minutesInHour
            report.balance shouldBe 0
        }

        "single incomplete work day" {
            val report = calculate("1.6.21") {
                day("1.6.21") {
                    "10-16" about "any"
                }
            }

            report.totalMinutesToWork shouldBe 8 * minutesInHour
            report.totalMinutesWorked shouldBe 6 * minutesInHour
            report.balance shouldBe -2 * minutesInHour
        }

        "filter weekend" {
            val report = calculate("5.6.21") {
                fullWorkingDay("4.6.21")
            }

            report.totalMinutesToWork shouldBe workMinutesPerDay
        }

        "filter free day" {
            val report = calculate("2.6.21", { freeDays += WorkDay.Wednesday }) {
                fullWorkingDay("1.6.21")
            }

            report.totalMinutesToWork shouldBe workMinutesPerDay
        }

        "filter day off" {
            val report = calculate("2.6.21") {
                fullWorkingDay("1.6.21")
                someDayOff("2.6.21")
            }

            report.totalMinutesToWork shouldBe (8 * minutesInHour)
            report.totalMinutesWorked shouldBe (8 * minutesInHour)
            report.balance shouldBe 0
        }
    }

    private fun calculate(today: String, initCode: TimeSheetInitDsl.() -> Unit = {}, sheet: TimeSheetDsl.() -> Unit) =
        TimeCalculator(clockReturning(today))
            .calculate(timesheet(initCode = initCode, entryCode = sheet))

    private fun clockReturning(date: String): Clock {
        val clock = mockk<Clock>()
        every { clock.currentLocalDate() } returns date.parseDate()
        return clock
    }
}
