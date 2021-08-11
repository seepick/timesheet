package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.DemoOffReasons.sickness
import com.github.cpickl.timesheet.DemoTags.biz
import com.github.cpickl.timesheet.DemoTags.code
import com.github.cpickl.timesheet.DemoTags.meet
import com.github.cpickl.timesheet.DemoTags.orga
import com.github.cpickl.timesheet.builder.OffReasons
import com.github.cpickl.timesheet.builder.WorkDayDsl
import com.github.cpickl.timesheet.builder.Tags
import com.github.cpickl.timesheet.builder.timesheet
import java.time.Month

// 1. a sample day
// ====================================================================================================================
fun main() {
    timesheet(DemoTags, DemoOffReasons, {
        daysOff += WorkDay.Friday
    }) {
        year(2021) {
            month(Month.JULY) {
                day(1) {
                    "9-" - "self admin" - orga
                    standup() // enhance DSL with custom extensions, nice :)
                    "-12:30" - "commons tests" - code
                    "13:30-" - "refine stories" - biz
                    "14:30-" - "commons tests" - code
                    "16-17" - "story alignment" - meet
                }
                dayOff(2) becauseOf sickness
            }
        }
    }.calculate().printCli()
}

// 2. define context
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

// (3. hook into main app)
// ====================================================================================================================
class MyAutoSheet : AutoSheet {
    override fun provide() = timesheet(DemoTags, DemoOffReasons) {
        // ... define your times here ...
    }

    override fun chooseReport(report: ReportContext) {
        report.printCli()
        // or: report.showNotification()
    }
}