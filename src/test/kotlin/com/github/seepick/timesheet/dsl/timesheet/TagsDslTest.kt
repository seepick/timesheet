package com.github.seepick.timesheet.dsl.timesheet

import com.github.seepick.timesheet.tags.tag
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.property.Arb
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.next

class TagsDslTest : DescribeSpec({

    val someDate = Arb.localDate().next()
    val tag1 = Arb.tag().next()
    val tag2 = Arb.tag().next()

    describe("tags") {
        it("two tags Then parsed tags returned") {
            val sheet = timesheetAny(someDate) {
                someWorkingDay(someDate) {
                    someWorkEntry(tags = listOf(tag1, tag2))
                }
            }
            sheet.entries.workEntries.shouldBeSingleton().first().tags shouldContainExactly setOf(tag1, tag2)
        }
    }
})
