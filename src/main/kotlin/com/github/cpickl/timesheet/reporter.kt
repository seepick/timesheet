package com.github.cpickl.timesheet

import java.text.DecimalFormat
import kotlin.math.abs


data class TimeReport(
    val totalMinutesToWork: Minutes,
    val totalMinutesWorked: Minutes,
) {
    private val hoursFormatter = DecimalFormat("##.#")

    val balance: Minutes = totalMinutesWorked - totalMinutesToWork
    private val balanceState = when {
        balance < 0.0 -> BalanceState.ToLittle
        balance > 0.0 -> BalanceState.Surplus
        else -> BalanceState.ExactMatch
    }

    private val hoursBalance: Double = balance.toDouble() / 60.0
    private val hoursBalanceFormatted = hoursFormatter.format(hoursBalance)
    private val absHoursBalanceFormatted = hoursFormatter.format(abs(hoursBalance))

    val hoursBalanceString = when(balanceState) {
        BalanceState.ToLittle -> "In ${"MINUS".colorize} of [$absHoursBalanceFormatted] hours ⚠️"
        BalanceState.Surplus -> "${"SURPLUS".colorize} of [$hoursBalanceFormatted] hours ❤️"
        BalanceState.ExactMatch -> "Exact ${"MATCH".colorize} of working hours 🦄"
    }

    private val String.colorize: String get() = balanceState.wrap(this)
}

private enum class BalanceState {
    ToLittle {
        override val wrapColor=PrintSymbols.ANSI_RED
    },
    Surplus {
        override val wrapColor=PrintSymbols.ANSI_GREEN
    },
    ExactMatch {
        override val wrapColor=PrintSymbols.ANSI_CYAN
    };

    protected abstract val wrapColor: String

    fun wrap(message: String) = "$wrapColor$message${PrintSymbols.ANSI_RESET}"
}

private object PrintSymbols {
    const val ANSI_RESET = "\u001B[0m"
    const val ANSI_BLACK = "\u001B[30m"
    const val ANSI_RED = "\u001B[31m"
    const val ANSI_GREEN = "\u001B[32m"
    const val ANSI_YELLOW = "\u001B[33m"
    const val ANSI_BLUE = "\u001B[34m"
    const val ANSI_PURPLE = "\u001B[35m"
    const val ANSI_CYAN = "\u001B[36m"
    const val ANSI_WHITE = "\u001B[37m"
}
