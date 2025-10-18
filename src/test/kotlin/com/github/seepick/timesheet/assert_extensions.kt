package com.github.seepick.timesheet

import com.github.seepick.timesheet.builder.BuilderException
import com.github.seepick.timesheet.builder.TimeSheetDsl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.time.LocalDate

infix fun TimeSheet.shouldHaveSingleEntryWithDate(expected: LocalDate) {
    entries.size shouldBe 1
    entries.first().day shouldBe expected
}

fun failingTimesheet(dsl: TimeSheetDsl.() -> Unit): BuilderException =
    shouldThrow {
        timesheetAny(entryCode = dsl)
    }

