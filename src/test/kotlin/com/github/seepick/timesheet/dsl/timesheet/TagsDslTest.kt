package com.github.seepick.timesheet.dsl.timesheet

import com.github.seepick.timesheet.tags.Tag
import com.github.seepick.timesheet.tags.tag1
import com.github.seepick.timesheet.tags.tag2
import com.github.seepick.timesheet.test_infra.TestConstants
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly

class TagsDslTest : DescribeSpec({

    val someDate = TestConstants.someDate
    val tag1 = Tag.tag1
    val tag2 = Tag.tag2

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
