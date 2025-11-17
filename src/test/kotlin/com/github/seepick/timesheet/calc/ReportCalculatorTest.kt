package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.MINUTES_IN_HOUR
import com.github.seepick.timesheet.date.ReportView
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
import com.github.seepick.timesheet.report.calculate
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Month
import java.time.Year
import java.time.YearMonth

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
        ReportCalculator().calculate(
            sheet = timesheetAny(today = today.parseDate(), entryCode = sheet),
            reportView = reportView,
            today = today.parseDate(),
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

        it("several views") {
            val today = "1.2.$yearShort"

            val reports = timesheetAny(today) {
                year(year) {
                    month(1) {
                        day(1) {
                            contract {
                                hoursPerWeek = 5
                            }
                            "10-11" about "any"
                        }
                    }
                }
            }.calculate(today.parseDate()).reportDatas

            reports.shouldHaveSize(3)
            reports[0].reportView.shouldBeInstanceOf<ReportView.MonthReportView>()
                .yearMonth shouldBe YearMonth.of(year, 1)
            reports[0].totalHoursWorked shouldBe 1.0
            reports[0].totalHoursToWork shouldBe 21.0
            reports[0].balanceInHours shouldBe -20.0

            reports[1].reportView.shouldBeInstanceOf<ReportView.MonthReportView>()
                .yearMonth shouldBe YearMonth.of(year, 2)
            reports[1].totalHoursWorked shouldBe 0.0
            reports[1].totalHoursToWork shouldBe 1.0
            reports[1].balanceInHours shouldBe -1.0

            reports[2].reportView.shouldBeInstanceOf<ReportView.YearReportView>()
                .year shouldBe Year.of(year)
            reports[2].totalHoursWorked shouldBe 1.0
            reports[2].totalHoursToWork shouldBe 22.0
            reports[2].balanceInHours shouldBe -21.0
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

