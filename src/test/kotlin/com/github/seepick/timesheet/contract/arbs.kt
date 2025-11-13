package com.github.seepick.timesheet.contract

import com.github.seepick.timesheet.date.workDays
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next

fun Arb.Companion.workContract() = arbitrary {
    WorkContract(
        daysOff = workDays().next(),
        hoursPerWeek = int(min = 10, max = 50).next()
    )
}
