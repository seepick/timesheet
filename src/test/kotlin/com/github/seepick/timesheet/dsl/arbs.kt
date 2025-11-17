package com.github.seepick.timesheet.dsl

import com.github.seepick.timesheet.date.timeRangeSpec
import com.github.seepick.timesheet.off.BuilderDayOffEntry
import com.github.seepick.timesheet.off.BuilderDaysOffEntry
import com.github.seepick.timesheet.off.offReason
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.intRange
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import java.time.Month

fun Arb.Companion.builderEntry(): Arb<BuilderEntry> = when (int(1..3).next()) {
    1 -> builderWorkDayEntry()
    2 -> builderDayOffEntry()
    else -> builderDaysOffEntry()
}

fun Arb.Companion.builderWorkDayEntry() = arbitrary {
    BuilderWorkDayEntry(
        day = localDate().next(),
        timeRangeSpec = timeRangeSpec().next(),
        about = string().next(),
    )
}

fun Arb.Companion.builderDayOffEntry() = arbitrary {
    BuilderDayOffEntry(
        day = localDate().next(),
        reason = offReason().orNull().next(),
    )
}

fun Arb.Companion.builderDaysOffEntry() = arbitrary {
    BuilderDaysOffEntry(
        year = 2000 + int(0..100).next(),
        month = enum<Month>().next(),
        days = intRange(1..10).next()
    )
}
