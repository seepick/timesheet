package com.github.cpickl.timesheet.report

import com.github.cpickl.timesheet.BalanceState
import com.github.cpickl.timesheet.Reporter
import com.github.cpickl.timesheet.TimeReportData
import java.text.DecimalFormat
import kotlin.math.abs

class SimpleCliReporter : Reporter {

    private val hoursFormatter = DecimalFormat("##.#")

    override fun report(data: TimeReportData) {
        println(generateHoursBalanceString(data))
    }

    private fun generateHoursBalanceString(data: TimeReportData): String {
        fun String.colorize() = data.balanceState.wrap(this)
        return when (data.balanceState) {
            BalanceState.ToLittle -> {
                val absHoursBalanceFormatted = hoursFormatter.format(abs(data.balanceInHours))
                "In ${"MINUS".colorize()} of [$absHoursBalanceFormatted] hours ⚠️"
            }
            BalanceState.Surplus -> {
                val hoursBalanceFormatted = hoursFormatter.format(data.balanceInHours)
                "${"SURPLUS".colorize()} of [$hoursBalanceFormatted] hours ❤️"
            }
            BalanceState.ExactMatch -> "Exact ${"MATCH".colorize()} of working hours 🦄"
        }
    }

    private fun BalanceState.wrap(message: String) = "$wrapColor$message${PrintSymbols.ANSI_RESET}"
}

private val BalanceState.wrapColor
    get() = when (this) {
        BalanceState.ToLittle -> PrintSymbols.ANSI_RED
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
