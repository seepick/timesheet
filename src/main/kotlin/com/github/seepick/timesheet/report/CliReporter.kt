package com.github.seepick.timesheet.report

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.ReportView
import com.github.seepick.timesheet.date.format
import java.lang.Math.abs
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class CliReporter : Reporter {

    private val hoursFormatter = DecimalFormat("##.#")

    override fun report(context: ReportContext, today: LocalDate) {
        context.reportDatas.forEach {
            reportReport(it, today)
        }
    }

    private fun reportReport(data: TimeReportData, today: LocalDate) {
        val title = when (data.reportView) {
            is ReportView.TotalReportView -> "Total"
            is ReportView.YearReportView -> data.reportView.year.toString()
            is ReportView.MonthReportView ->
                data.reportView.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        }
        println("$title Report (${data.reportView.dateRange.limitedEndBy(today).format()}):")
        println(generateHoursBalanceString(data))
        println()
    }

    private fun generateHoursBalanceString(data: TimeReportData): String {
        val pluralS = if (data.balanceInHours == 1.0) "" else "s"
        return when (data.balanceState) {
            BalanceState.TooLittle -> {
                val absHoursBalanceFormatted = hoursFormatter.format(abs(data.balanceInHours))
                "In ${data.balanceState.wrap("MINUS")} of [$absHoursBalanceFormatted] hour$pluralS ⚠️"
            }

            BalanceState.Surplus -> {
                val hoursBalanceFormatted = hoursFormatter.format(data.balanceInHours)

                "${data.balanceState.wrap("SURPLUS")} of [$hoursBalanceFormatted] hour$pluralS ❤️"
            }

            BalanceState.ExactMatch -> "Exact ${data.balanceState.wrap("MATCH")} of working hours 🦄"
        }
    }

}

private fun DateRange.format(): String {
    return "${startDate.format()} to ${endDate.format()}"
}

private fun BalanceState.wrap(message: String) = "$wrapColor$message${PrintSymbols.ANSI_RESET}"

private val BalanceState.wrapColor
    get() = when (this) {
        BalanceState.TooLittle -> PrintSymbols.ANSI_RED
        BalanceState.Surplus -> PrintSymbols.ANSI_GREEN
        BalanceState.ExactMatch -> PrintSymbols.ANSI_CYAN
    }

private object PrintSymbols {
    const val ANSI_RESET = "\u001B[0m"
    const val ANSI_RED = "\u001B[31m"
    const val ANSI_GREEN = "\u001B[32m"
    const val ANSI_CYAN = "\u001B[36m"

//    const val ANSI_BLACK = "\u001B[30m"
//    const val ANSI_YELLOW = "\u001B[33m"
//    const val ANSI_BLUE = "\u001B[34m"
//    const val ANSI_PURPLE = "\u001B[35m"
//    const val ANSI_WHITE = "\u001B[37m"
}
