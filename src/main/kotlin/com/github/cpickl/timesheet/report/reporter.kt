package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.report.NotificationReporter
import com.github.cpickl.timesheet.report.SimpleCliReporter

class ReportContext(
    private val sheet: TimeSheet,
    private val reportData: TimeReportData,
) {

    fun printCli() =
        execute(SimpleCliReporter())

    // TODO displays: yesterday's balance, total balance (CLI+NOTIFY)
    // fun printCliDetails() {}

    fun showNotification() =
        execute(NotificationReporter())

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
