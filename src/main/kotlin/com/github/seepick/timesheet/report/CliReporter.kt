package com.github.seepick.timesheet.report

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.format
import java.lang.Math.abs
import java.text.DecimalFormat

class CliReporter : Reporter {

    private val hoursFormatter = DecimalFormat("##.#")

    override fun report(context: ReportContext) {
        context.reportDatas.forEach {
            reportReport(it)
        }
    }

    private fun reportReport(data: TimeReportData) {
        val title = when (data.reportView.rangeType) {
            ReportRangeType.Total -> "Total"
            ReportRangeType.Yearly -> "Yearly"
            ReportRangeType.Monthly -> "Monthly"
        }
        println("$title Report (${data.reportView.dateRange.format()}):")
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
