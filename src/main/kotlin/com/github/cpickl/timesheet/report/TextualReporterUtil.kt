package com.github.cpickl.timesheet.report

import com.github.cpickl.timesheet.BalanceState
import com.github.cpickl.timesheet.TimeReportData
import java.text.DecimalFormat
import kotlin.math.abs

object TextualReporterUtil {

    private val hoursFormatter = DecimalFormat("##.#")

    fun generateHoursBalanceString(data: TimeReportData, colorize: String.() -> String): String {
        val pluralS = if(data.balanceInHours == 1.0) "" else "s"
        return when (data.balanceState) {
            BalanceState.ToLittle -> {
                val absHoursBalanceFormatted = hoursFormatter.format(abs(data.balanceInHours))
                "In ${"MINUS".colorize()} of [$absHoursBalanceFormatted] hour$pluralS ⚠️"
            }
            BalanceState.Surplus -> {
                val hoursBalanceFormatted = hoursFormatter.format(data.balanceInHours)

                "${"SURPLUS".colorize()} of [$hoursBalanceFormatted] hour$pluralS ❤️"
            }
            BalanceState.ExactMatch -> "Exact ${"MATCH".colorize()} of working hours 🦄"
        }
    }

}