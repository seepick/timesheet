package com.github.seepick.timesheet

import com.github.seepick.timesheet.DemoOffReasons.sickness
import com.github.seepick.timesheet.DemoTags.biz
import com.github.seepick.timesheet.DemoTags.code
import com.github.seepick.timesheet.DemoTags.meet
import com.github.seepick.timesheet.DemoTags.orga
import com.github.seepick.timesheet.date.WorkDay.friday
import com.github.seepick.timesheet.date.WorkDay.tuesday
import com.github.seepick.timesheet.date.monday
import com.github.seepick.timesheet.date.november
import com.github.seepick.timesheet.date.rd
import com.github.seepick.timesheet.date.th
import com.github.seepick.timesheet.dsl.WorkDayDsl
import com.github.seepick.timesheet.dsl.timesheet
import com.github.seepick.timesheet.off.NamedOffReason
import com.github.seepick.timesheet.off.OffReasons
import com.github.seepick.timesheet.report.calculate
import com.github.seepick.timesheet.tags.NamedTag
import com.github.seepick.timesheet.tags.Tags

// 1. define context
// ====================================================================================================================

private object DemoTags : Tags {
    val orga = NamedTag("Organisational")
    val code = NamedTag("Coding")
    val biz = NamedTag("Business")
    val meet = NamedTag("Meeting")

    override fun all() = listOf(orga, code, biz, meet)
}

private object DemoOffReasons : OffReasons {
    val publicHoliday = NamedOffReason("Public Holiday")
    val sickness = NamedOffReason("Sickness")
    val vacation = NamedOffReason("Vacation")

    override fun all() = listOf(publicHoliday, sickness, vacation)
}

private fun WorkDayDsl.standup() {
    "10-10:30" - "standup" - meet
}

// 2. track times and print report
// ====================================================================================================================
fun main() {
    timesheet(DemoTags, DemoOffReasons) {
        year(2025) {
            november {
                monday(3.rd) {
                    contract {
                        hoursPerWeek = 32
                        dayOff = friday
                    }
                    "9-" - "self admin" - orga
                    standup() // enhance DSL with custom extensions, nice :)
                    "-12:30" - "commons tests" - code
                    "13:30-" - "refine stories" - biz
                    "14:30-" - "commons tests" - code
                    "16-17" - "story alignment" - meet
                }
                dayOff(tuesday, 4.th) becauseOf sickness
            }
        }
    }.calculate().printCli()
}
