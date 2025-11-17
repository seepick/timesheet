package com.github.seepick.timesheet.dsl

import com.github.seepick.timesheet.contract.DefinedWorkContract
import com.github.seepick.timesheet.contract.WorkContract
import com.github.seepick.timesheet.contract.definedWorkContract
import com.github.seepick.timesheet.date.TimeRangeSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.next
import java.time.LocalDate

class CurrentTest : DescribeSpec({

    fun anyDate() = Arb.localDate().next()
    fun anyBuilderEntry() = Arb.builderEntry().next()
    val someDate = Arb.localDate().next()
    val someDefinedWorkContract = Arb.definedWorkContract().next()
    fun workDayWithDayAndTime(day: LocalDate, timeSpec: String) = Arb.builderWorkDayEntry().next()
        .copy(day = day, timeRangeSpec = TimeRangeSpec.parse(timeSpec))

    describe("addDefaultContractIfEmpty") {
        it("Given not empty Then do nothing") {
            val current = Current()
            current.contracts += someDefinedWorkContract
            current.addDefaultContractIfEmpty(anyDate())
            current.contracts.shouldBeSingleton().first().shouldBeSameInstanceAs(someDefinedWorkContract)
        }
        it("Given empty Then add default contract") {
            val current = Current()
            current.addDefaultContractIfEmpty(someDate)
            current.contracts.shouldBeSingleton().first() shouldBeEqual
                    DefinedWorkContract(WorkContract.default, someDate)
        }
    }
    describe("throwOnDuplicateDaysOffEntries") {
        it("When empty Then do nothing") {
            Current().throwOnDuplicateDaysOffEntries()
        }
        it("When single Then do nothing") {
            val current = Current()
            current.entries += anyBuilderEntry()

            current.throwOnDuplicateDaysOffEntries()
        }
        it("When work days on same date and time Then do nothing as checking only for days off") {
            val current = Current()
            current.entries += workDayWithDayAndTime(someDate, "8-9")
            current.entries += workDayWithDayAndTime(someDate, "8-9")

            current.throwOnDuplicateDaysOffEntries()
        }
        it("When work days on same date but different time Then do nothing") {
            val current = Current()
            current.entries += workDayWithDayAndTime(someDate, "8-9")
            current.entries += workDayWithDayAndTime(someDate, "10-11")

            current.throwOnDuplicateDaysOffEntries()
        }
        it("When days off on same date Then throw") {
            val current = Current()
            current.entries += Arb.builderDayOffEntry().next().copy(day = someDate)
            current.entries += Arb.builderDayOffEntry().next().copy(day = someDate)

            shouldThrow<InvalidEntriesException> {
                current.throwOnDuplicateDaysOffEntries()
            }.message shouldContain "Found duplicates"
        }
        it("When work day and day off on same date Then throw") {
            val current = Current()
            current.entries += Arb.builderWorkDayEntry().next().copy(day = someDate)
            current.entries += Arb.builderDayOffEntry().next().copy(day = someDate)

            shouldThrow<InvalidEntriesException> {
                current.throwOnDuplicateDaysOffEntries()
            }.message shouldContain "Found duplicates"
        }
    }
})
