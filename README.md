# Timesheet

Keep track of your working hours with an elegant **Kotlin DSL**.

*No spreadsheet. No text file. Just wonderful code!*

# Usage

```kotlin
// a sample day
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

Simply create a (already GIT ignored) file at `src/main/kotlin/com/github/cpickl/timesheet/MyTimeSheetApp.kt` and fill it with that content.

## Auto

When running the `TimesheetApp` (via gradle from a cronjob) it will look for class named `com.github.cpickl.timesheet.MyAutoSheet` to hook it in.

Your (GIT ignored) file should contain the following to enagble integration:

```kotlin
class MyAutoSheet : AutoSheet {
    override fun provide() = timesheet(DemoTags, DemoOffReasons) {
        // ... define your times here ...
    }

    override fun chooseReport(report: ReportContext) {
        report.printCli()
        // or: report.showNotification()
    }
}
```
