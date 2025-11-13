package com.github.seepick.timesheet.contract

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.timeRange
import com.github.seepick.timesheet.timesheet.EntryDateRange
import com.github.seepick.timesheet.timesheet.TimeEntries
import com.github.seepick.timesheet.timesheet.WorkDayEntry
import com.github.seepick.timesheet.timesheet.byTimeEntries
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import java.time.LocalDate

class TransformContractTest : StringSpec() {
    init {
        "Given single contract When check range Then is valid until last work entry" {
            val contract = Arb.workContract().next()
            val contractDefinedAt = LocalDate.parse("2025-11-01")
            val workEntryDay = LocalDate.parse("2025-11-05")
            val rangedWorkContracts = transformContracts(
                contracts = listOf(DefinedWorkContract(contract, definedAt = contractDefinedAt)),
                timeEntries = TimeEntries.byTimeEntries(listOf(workEntry(workEntryDay)))
            )

            rangedWorkContracts.shouldBeSingleton().first() shouldBe RangedWorkContract(
                contract, DateRange(startDate = contractDefinedAt, endDate = workEntryDay)
            )
        }
        "Given two contracts When check range Then calc dates properly" {
            val contract1 = Arb.workContract().next()
            val contract2 = Arb.workContract().next()
            val contract1DefinedAt = LocalDate.parse("2025-11-01")
            val contract2DefinedAt = LocalDate.parse("2025-11-03")
            val workEntryDay = LocalDate.parse("2025-11-05")

            val rangedWorkContracts = transformContracts(
                contracts = listOf(
                    DefinedWorkContract(contract1, definedAt = contract1DefinedAt),
                    DefinedWorkContract(contract2, definedAt = contract2DefinedAt),
                ),
                timeEntries = TimeEntries.byTimeEntries(listOf(workEntry(workEntryDay)))
            )

            rangedWorkContracts.shouldHaveSize(2)
            rangedWorkContracts[0] shouldBe RangedWorkContract(
                contract1,
                DateRange(startDate = contract1DefinedAt, endDate = contract2DefinedAt.minusDays(1))
            )
            rangedWorkContracts[1] shouldBe RangedWorkContract(
                contract2,
                DateRange(startDate = contract2DefinedAt, endDate = workEntryDay)
            )
        }
    }

    private fun workEntry(day: LocalDate) = WorkDayEntry(
        dateRange = EntryDateRange(
            day = day,
            timeRange = Arb.timeRange().next()
        ), about = "", tags = setOf()
    )
}
