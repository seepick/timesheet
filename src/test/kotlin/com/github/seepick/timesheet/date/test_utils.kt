package com.github.seepick.timesheet.date

import java.time.Year
import java.time.YearMonth

fun generateViews(start: String, end: String) = generateReportViews(start.parseDate(), end.parseDate())
fun totalView(start: String, end: String) = ReportView.TotalReportView(DateRange(start.parseDate(), end.parseDate()))
fun yearView(year: Int) = ReportView.YearReportView(Year.of(year))
fun monthView(year: Int, month: Int) = ReportView.MonthReportView(YearMonth.of(year, month))
