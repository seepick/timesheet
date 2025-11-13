package com.github.seepick.timesheet.dsl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class BuilderTransformerKtTest : DescribeSpec() {
    init {
        describe("fo") {
            it("sd") {
                val workEntry = Arb.builderWorkDayEntry().next()
                
//                BuilderEntry.toRealEntry(neighbours: Pair<BuilderEntry?, BuilderEntry?>): List<TimeEntry> =
            }
        }
    }
}
