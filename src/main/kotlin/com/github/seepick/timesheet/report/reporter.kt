package com.github.seepick.timesheet.report

import com.github.seepick.timesheet.date.Hours
import com.github.seepick.timesheet.date.Minutes
import com.github.seepick.timesheet.calc.ReportCalculator
import com.github.seepick.timesheet.timesheet.TimeSheet

class ReportContext(
    private val sheet: TimeSheet,
    private val reportData: TimeReportData,
) {

    fun printCli() =
        execute(SimpleCliReporter())

    private fun execute(reporter: Reporter): ReportContext {
        reporter.report(reportData)
        return this
    }

}

private val calculator = ReportCalculator()

fun TimeSheet.calculate() = ReportContext(this, calculator.calculate(this))

interface Reporter {
    fun report(data: TimeReportData)
}

data class TimeReportData(
    val sheet: TimeSheet,
    val totalMinutesToWork: Minutes,
    val totalMinutesWorked: Minutes,
) {
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
