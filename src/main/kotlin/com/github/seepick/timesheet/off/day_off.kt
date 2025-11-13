package com.github.seepick.timesheet.off

import com.github.seepick.timesheet.timesheet.TimeEntry
import java.time.LocalDate

interface OffReason {
    val label: String

    companion object
}

class NamedOffReason(
    override val label: String
) : OffReason

data class DayOffEntry(
    override val day: LocalDate,
    val reason: OffReason,
) : TimeEntry
