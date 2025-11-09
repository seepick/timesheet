package com.github.seepick.timesheet.report

class SimpleCliReporter : Reporter {
    override fun report(data: TimeReportData) {
        println(TextualReporterUtil.generateHoursBalanceString(data) {
            data.balanceState.wrap(this)
        })
    }

    private fun BalanceState.wrap(message: String) = "$wrapColor$message${PrintSymbols.ANSI_RESET}"
}

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
