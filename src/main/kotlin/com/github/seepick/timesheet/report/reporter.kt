package com.github.seepick.timesheet.report

import com.github.seepick.timesheet.calc.ReportCalculator
import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.Hours
import com.github.seepick.timesheet.date.MINUTES_IN_HOUR
import com.github.seepick.timesheet.date.Minutes
import com.github.seepick.timesheet.date.ReportView
import com.github.seepick.timesheet.date.SystemClock
import com.github.seepick.timesheet.date.generateReportViews
import com.github.seepick.timesheet.tags.Tag
import com.github.seepick.timesheet.timesheet.TimeSheet
import java.time.LocalDate

data class ReportContext(
    val sheet: TimeSheet,
    val reportDatas: List<TimeReportData>,
) {
    fun printCli() {
        CliReporter().report(this)
    }
}

private val calculator = ReportCalculator()

fun TimeSheet.calculate(today: LocalDate = SystemClock.currentLocalDate()): ReportContext {
    val reportViews = generateReportViews(startDate, today)
    return ReportContext(
        sheet = this,
        reportDatas = reportViews.map {
            calculator.calculate(this, it, today)
        }
    )
}

interface Reporter {
    fun report(context: ReportContext, today: LocalDate = SystemClock.currentLocalDate())
}

enum class ReportRangeType {
    Total, Yearly, Monthly
}

sealed interface ReportRange {
    val dateRange: DateRange
    val type: ReportRangeType

    data class TotalReportRange(override val dateRange: DateRange) : ReportRange {
        override val type = ReportRangeType.Total
    }

    class YearlyReportRange(override val dateRange: DateRange) : ReportRange {
        override val type = ReportRangeType.Yearly
    }

    class MonthlyReportRange(override val dateRange: DateRange) : ReportRange {
        override val type = ReportRangeType.Monthly
    }
}

data class TagsReport(
    val minutesPerTag: Map<Tag, Minutes>
) {
    init {
        // FIXME tags calc percentages Map<Tag, Int>
        // percentagesPerTag
    }
}

data class TimeReportData(
    val reportView: ReportView,
    val tagsReport: TagsReport,
    val totalMinutesToWork: Minutes,
    val totalMinutesWorked: Minutes,
) {

    val totalHoursToWork = totalMinutesToWork.toDouble() / MINUTES_IN_HOUR
    val totalHoursWorked = totalMinutesWorked.toDouble() / MINUTES_IN_HOUR
    val balanceInMinutes: Minutes = totalMinutesWorked - totalMinutesToWork
    val balanceInHours: Hours = balanceInMinutes.toDouble() / 60.0

    val balanceState = when {
        balanceInMinutes < 0 -> BalanceState.TooLittle
        balanceInMinutes > 0 -> BalanceState.Surplus
        else -> BalanceState.ExactMatch
    }
}

enum class BalanceState {
    TooLittle,
    Surplus,
    ExactMatch;
}
