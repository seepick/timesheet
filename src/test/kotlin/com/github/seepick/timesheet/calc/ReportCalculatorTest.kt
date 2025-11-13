package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.MINUTES_IN_HOUR
import com.github.seepick.timesheet.date.StaticClock
import com.github.seepick.timesheet.date.WorkDay.wednesday
import com.github.seepick.timesheet.date.june
import com.github.seepick.timesheet.date.parseDate
import com.github.seepick.timesheet.date.tuesday
import com.github.seepick.timesheet.dsl.TimeSheetDsl
import com.github.seepick.timesheet.dsl.timesheet.someDayOff
import com.github.seepick.timesheet.dsl.timesheet.someWorkEntry
import com.github.seepick.timesheet.dsl.timesheet.timesheetAny
import com.github.seepick.timesheet.report.TimeReportData
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Month

class ReportCalculatorTest : StringSpec() {

    private val anyDay = 1
    private val workHoursPerDay = 8L
    private val workMinutesPerDay = workHoursPerDay * MINUTES_IN_HOUR

    init {
        "single complete work day" {
            val report = calculate("1.6.21") { // a tuesday
                year(2021) {
                    month(Month.JUNE) {
                        day(1) {
                            "10-18" about "any"
                        }
                    }
                }
            }

            report.totalHoursToWork shouldBe 8.0
            report.totalHoursWorked shouldBe 8.0
            report.balanceInHours shouldBe 0.0
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

            report.totalHoursToWork shouldBe 8.0
            report.totalHoursWorked shouldBe 6.0
            report.balanceInHours shouldBe -2.0
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

        "Given day off by contract When report including day off Then calc hours considering free day" {
            val report = calculate("2.6.21") { // next day is wednesday, which is off
                year(2021) {
                    june {
                        tuesday(1) {
                            contract {
                                hoursPerWeek = 32
                                dayOff = wednesday
                            }
                            someWorkEntry()
                        }
                    }
                }
            }

            report.totalHoursToWork shouldBe 8.0 // not 16
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

            report.totalHoursToWork shouldBe 8.0
            report.totalHoursWorked shouldBe 8.0
            report.balanceInHours shouldBe 0.0
        }
    }

    private fun calculate(today: String, sheet: TimeSheetDsl.() -> Unit): TimeReportData {
        val clock = StaticClock(today)
        return ReportCalculator(clock)
            .calculate(timesheetAny(today = today.parseDate(), entryCode = sheet))
    }
}
