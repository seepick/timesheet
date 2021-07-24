package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.DayDsl
import com.github.cpickl.timesheet.builder.BuilderTag
import com.github.cpickl.timesheet.builder.timesheet
import java.time.Month

fun main() {
    timesheet {
        year(2021) {
            month(Month.JULY) {
                day(1) {
                    "9-" - "self admin" - BuilderTag.orga
                    standup() // enhance DSL with custom extensions, nice :)
                    "-12:30" - "commons tests" - BuilderTag.code
                    "13:30-" - "refine stories" - BuilderTag.biz
                    "14:30-" - "commons tests" - BuilderTag.code
                    "16-17" - "story alignment" - BuilderTag.meet
                }
            }
        }
    }.printCli()
}

private fun DayDsl.standup() {
    "10-10:30" - "standup" - BuilderTag.meet
}
class MyTimeSheet : TimeSheetProvider {
    override fun provide() = timesheet {
        // ... define your times here ...
    }

    override fun chooseReport(report: ReportContext) {
        report.printCli()
//        report.showNotification()
    }
}