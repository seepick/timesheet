@file:JvmName("Logic")

package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.MINUTES_IN_HOUR
import com.github.seepick.timesheet.date.ReportView
import com.github.seepick.timesheet.date.isWeekDay
import com.github.seepick.timesheet.off.DayOffEntry
import com.github.seepick.timesheet.report.TimeReportData
import com.github.seepick.timesheet.timesheet.TimeSheet
import java.time.LocalDate
import kotlin.math.roundToLong

class ReportCalculator {

    fun calculate(sheet: TimeSheet, reportView: ReportView, today: LocalDate): TimeReportData {
        return TimeReportData(
            reportView = reportView,
            tagsReport = calculateTags(reportView, sheet),
            totalMinutesToWork = sheet.calcTotalMinutesToWork(reportView, today),
            totalMinutesWorked = sheet.calcTotalMinutesWorked(reportView),
        )
    }

    private fun TimeSheet.calcTotalMinutesWorked(reportView: ReportView): Long {
        return entries.workEntries.filter(reportView::filter).sumOf { it.duration }
    }

    private fun TimeSheet.calcTotalMinutesToWork(reportView: ReportView, today: LocalDate): Long {
        val dayOffEntries = this.entries.filterIsInstance<DayOffEntry>().map { it.day }.toSet()
        val totalHoursToWork = DateRange(startDate, today).limitedBy(reportView.dateRange)
            .mapNotNull { this.hoursToWorkOrNull(it, dayOffEntries) }
            .sum()
        return (totalHoursToWork * MINUTES_IN_HOUR).roundToLong()
    }

    private fun TimeSheet.hoursToWorkOrNull(currentDate: LocalDate, dayOffEntries: Set<LocalDate>): Double? {
        if (!currentDate.dayOfWeek.isWeekDay) return null
        val contract = this.contractFor(currentDate)
        if (contract.javaDaysOff.contains(currentDate.dayOfWeek)) return null
        if (dayOffEntries.contains(currentDate)) return null
        return contract.hoursPerDay
    }
}

