package com.github.seepick.timesheet.report

import com.github.seepick.timesheet.Hours
import com.github.seepick.timesheet.Minutes
import com.github.seepick.timesheet.TimeCalculator
import com.github.seepick.timesheet.TimeSheet

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

private val calculator = TimeCalculator()

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
        balanceInMinutes < 0 -> BalanceState.ToLittle
        balanceInMinutes > 0 -> BalanceState.Surplus
        else -> BalanceState.ExactMatch
    }
}

enum class BalanceState {
    ToLittle,
    Surplus,
    ExactMatch;
}
