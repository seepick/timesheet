# Timesheet

Keep track of your working hours with an elegant **Kotlin DSL**.

*No spreadsheet. No text file. Just wonderful code!*

```kotlin
timesheet({
    daysOff += WorkDay.Friday
}) {
    year(2021) {
        month(6) {
            day(1) {
                "9-" - "self admin" - TagDso.orga
                standup() // enhance DSL with custom extensions, nice :)
                "-12:30" - "commons tests" - TagDso.code
                "13:30-" - "refine stories" - TagDso.biz
                "14:30-" - "commons tests" - TagDso.code
                "16-17" - "story alignment" - TagDso.meet
            }
        }
    }
}

private fun DayDsl.standup() {
    "10-10:30" - "standup" - meet
}
```

## Setup

When running the `TimesheetApp` it will look for (GIT ignored) file at: `src/main/kotlin/com/github/cpickl/timesheet/MyTimeSheet.kt`

Create it and add the following:

```kotlin
package com.github.cpickl.timesheet

class MyTimeSheet : TimeSheetProvider {
    override fun provide() = timesheet {
        // ... define your times here ...
    }

    override fun chooseReport(report: ReportContext) {
        report.printCli()
//        report.showNotification()
    }
}
```
