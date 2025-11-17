@file:JvmName("GenerateReportViews")

package com.github.seepick.timesheet.date

import com.github.seepick.timesheet.report.ReportRangeType
import com.github.seepick.timesheet.timesheet.TimeEntry
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

fun generateReportViews(start: LocalDate, end: LocalDate): List<ReportView> {
    require(start <= end) { "start [$start] must be <= end [$end]" }
    val startYearMonth = start.toYearMonth()
    val endYearMonth = end.toYearMonth()
    val result = mutableListOf<ReportView>()

    if (startYearMonth == endYearMonth) {
        result += ReportView.MonthReportView(startYearMonth)
    } else if (start.year == end.year) {
        result += generateYear(startYearMonth, endYearMonth)
    } else {
        result += generateTotal(startYearMonth, endYearMonth)
    }
    return result
}

private fun generateTotal(startYearMonth: YearMonth, endYearMonth: YearMonth): List<ReportView> {
    val result = mutableListOf<ReportView>()
    (startYearMonth.year..endYearMonth.year).map { year ->
        result += generateYear(
            start = if (year == startYearMonth.year) startYearMonth else YearMonth.of(year, 1),
            end = if (year == endYearMonth.year) endYearMonth else YearMonth.of(year, 12),
        )
    }
    result += ReportView.TotalReportView(DateRange(startYearMonth.atBeginOfMonth(), endYearMonth.atEndOfMonth()))
    return result
}

private fun generateYear(start: YearMonth, end: YearMonth): List<ReportView> {
    val result = mutableListOf<ReportView>()
    var currentYearMonth = start
    val endYearMonth = end
    while (currentYearMonth <= endYearMonth) {
        result += ReportView.MonthReportView(currentYearMonth)
        currentYearMonth = currentYearMonth.plusMonths(1L)
    }
    result += ReportView.YearReportView(Year.of(start.year))
    return result
}

sealed interface ReportView {
    val dateRange: DateRange
    val rangeType: ReportRangeType
    fun filter(entry: TimeEntry) = dateRange.contains(entry.day)

    data class TotalReportView(
        override val dateRange: DateRange,
    ) : ReportView {
        override val rangeType = ReportRangeType.Total
    }

    data class YearReportView(val year: Year) : ReportView {
        override val rangeType = ReportRangeType.Yearly
        override val dateRange = DateRange(
            LocalDate.of(year.value, 1, 1),
            LocalDate.of(year.value, 12, 31)
        )
    }

    data class MonthReportView(val yearMonth: YearMonth) : ReportView {
        override val rangeType = ReportRangeType.Monthly
        override val dateRange = DateRange(
            yearMonth.atBeginOfMonth(),
            yearMonth.atEndOfMonth()
        )
    }
}
