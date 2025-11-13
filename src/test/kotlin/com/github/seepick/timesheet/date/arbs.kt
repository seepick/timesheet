package com.github.seepick.timesheet.date

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.localTime
import io.kotest.property.arbitrary.next
import java.time.LocalTime

fun Arb.Companion.workDays() = arbitrary {
    val amount = int(min = 0, max = 3).next()
    WorkDay.all.shuffled().take(amount).toSet()
}

fun Arb.Companion.timeRange() = arbitrary {
    val hour = int(min = 0, max = 22).next()
    TimeRange(
        start = LocalTime.of(hour, 0),
        end = LocalTime.of(hour + 1, 0),
    )
}

fun Arb.Companion.timeRangeSpec() =
    when (int(1..3).next()) {
        1 -> closedRangeSpec()
        2 -> openEndRangeSpec()
        else -> openStartRangeSpec()
    }

fun Arb.Companion.closedRangeSpec() = arbitrary {
    val hour = int(min = 0, max = 22).next()
    ClosedRangeSpec(
        start = LocalTime.of(hour, 0),
        end = LocalTime.of(hour + 1, 0),
    )
}

fun Arb.Companion.openStartRangeSpec() = arbitrary {
    OpenStartRangeSpec(
        end = localTime().next(),
    )
}

fun Arb.Companion.openEndRangeSpec() = arbitrary {
    OpenEndRangeSpec(
        start = localTime().next(),
    )
}
