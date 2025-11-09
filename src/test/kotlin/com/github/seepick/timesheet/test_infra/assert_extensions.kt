package com.github.seepick.timesheet.test_infra

import com.github.seepick.timesheet.dsl.BuilderException
import com.github.seepick.timesheet.dsl.TimeSheetDsl
import com.github.seepick.timesheet.timesheet.TimeSheet
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

