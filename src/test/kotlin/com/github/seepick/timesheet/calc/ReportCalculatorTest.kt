package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.MINUTES_IN_HOUR
import com.github.seepick.timesheet.date.ReportView
import com.github.seepick.timesheet.date.StaticClock
import com.github.seepick.timesheet.date.WorkDay.wednesday
import com.github.seepick.timesheet.date.june
import com.github.seepick.timesheet.date.monthView
import com.github.seepick.timesheet.date.parseDate
import com.github.seepick.timesheet.date.tuesday
import com.github.seepick.timesheet.date.yearView
import com.github.seepick.timesheet.dsl.TimeSheetDsl
import com.github.seepick.timesheet.dsl.timesheet.someDayOff
import com.github.seepick.timesheet.dsl.timesheet.someWorkEntry
import com.github.seepick.timesheet.dsl.timesheet.someWorkingDay
import com.github.seepick.timesheet.dsl.timesheet.timesheetAny
import com.github.seepick.timesheet.report.TimeReportData
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Month

class ReportCalculatorTest : DescribeSpec({

    val year = 2021
    val yearShort = "21"
    val workHoursPerDay = 8L
    val workMinutesPerDay = workHoursPerDay * MINUTES_IN_HOUR
    val defaultReportView: ReportView = yearView(year)

    fun calculate(
        today: String,
        reportView: ReportView = defaultReportView,
        sheet: TimeSheetDsl.() -> Unit
    ): TimeReportData =
        ReportCalculator(StaticClock(today)).calculate(
            sheet = timesheetAny(today = today.parseDate(), entryCode = sheet),
            reportView = reportView,
        )

    describe("general") {
        it("single complete work day") {
            val report = calculate("1.6.$yearShort") { // a tuesday
                year(year) {
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

        it("single incomplete work day") {
            val report = calculate(today = "1.2.$yearShort") {
                year(year) {
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

        it("filter weekend") {
            val report = calculate("5.6.$yearShort") {
                year(year) {
                    month(Month.JUNE) {
                        day(4) {
                            "10-18" - "any"
                        }
                    }
                }
            }

            report.totalMinutesToWork shouldBe workMinutesPerDay
        }

        it("Given day off by contract When report including day off Then calc hours considering free day") {
            val report = calculate("2.6.$yearShort") { // next day is wednesday, which is off
                year(year) {
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

        it("filter day off") {
            val report = calculate("2.6.$yearShort") {
                year(year) {
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
    describe("report range") {
        it("monthly filter") {
            val report = calculate("3.2.$yearShort", monthView(year, 2)) {
                year(year) {
                    month(1) {
                        someWorkingDay(1)
                    }
                }
            }
            report.reportView.dateRange shouldBe DateRange("1.2.$yearShort" to "31.2.$yearShort")
            report.totalHoursWorked shouldBe 0.0
        }
        it("yearly filter") {
            val report = calculate("3.2.$yearShort") {
                year(year - 1) {
                    month(1) {
                        someWorkingDay(1)
                    }
                }
            }
            report.reportView.dateRange shouldBe DateRange("1.1.$yearShort" to "31.12.$yearShort")
            report.totalHoursWorked shouldBe 0.0 // work entry from previous year filtered out
        }
    }
})
