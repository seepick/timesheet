# Timesheet

Keep track of your working hours with an elegant **Kotlin DSL**.

*No spreadsheet. No text file. Just wonderful, sophisticated code supporting auto-completion and all the other amenities!*

# Usage

Simply create an already-GIT-ignored file at `src/main/kotlin/com/github/seepick/timesheet/MyTimeSheetApp.kt` and fill
it with the following sampple content:

```kotlin
package com.github.seepick.timesheet

import DemoTags.biz
import DemoTags.code
import DemoTags.meet
import DemoTags.orga
import com.github.seepick.timesheet.NamedOffReason
import com.github.seepick.timesheet.NamedTag
import com.github.seepick.timesheet.WorkDay
import com.github.seepick.timesheet.builder.OffReasons
import com.github.seepick.timesheet.builder.Tags
import com.github.seepick.timesheet.builder.WorkDayDsl
import com.github.seepick.timesheet.builder.timesheet
import com.github.seepick.timesheet.calculate
import java.time.Month

// a sample day
// ====================================================================================================================

fun main() {
    timesheet(DemoTags, DemoOffReasons, {
        daysOff += WorkDay.Friday
    }) {
        year(2025) {
            month(Month.OCTOBER) {
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

// define context
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
```
