package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.BuilderException
import com.github.cpickl.timesheet.builder.TimeSheetDsl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.time.LocalDate


infix fun TimeSheet.shouldHaveSingleEntryWithDate(expected: LocalDate) {
    entries.size shouldBe 1
    entries.first().day shouldBe expected
}

fun failingTimesheet(dsl: TimeSheetDsl.() -> Unit): BuilderException =
    shouldThrow {
        timesheet(entryCode = dsl)
    }

