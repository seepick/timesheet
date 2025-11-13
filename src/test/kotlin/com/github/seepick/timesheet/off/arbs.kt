package com.github.seepick.timesheet.off

import com.github.seepick.timesheet.timesheet.OffReason

object AnyOffReason : OffReason {
    override val label = "anyOffReason"
}

val OffReason.Companion.any get() = AnyOffReason

val OffReasons.Companion.any: OffReasons
    get() = object : OffReasons {
        override fun all() = listOf<OffReason>(AnyOffReason)
    }
