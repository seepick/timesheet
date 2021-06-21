# Timesheet

Keep track of your working hours by using a comfortable Kotlin DSL. No user interface, no command line, just code.

```kotlin
timesheet({
    daysOff += WorkDay.Friday
}) {
    day("1.6.21") {
        "9-10" - "self admin" - orga
        standup() // enhance DSL with custom extension functions; enjoy the full power of code!
        "10:30-12:30" - "commons tests" - code
        "13:30-14:30" - "refine stories" - biz
        "14:30-16" - "commons tests" - code
        "16-17" - "story alignment" - meet
    }
}

private fun DayDsl.standup() {
    "10-10:30" - "standup" - meet
}
```

## Setup

1. checkout the code
1. create a file `src/main/kotlin/com/github/cpickl/timesheet/MyTimeSheet.kt`
1. add file content:

```kotlin
package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.IntermediateTag.biz

class MyTimeSheet : TimeSheetProvider {
    override fun provide() = timesheet {
        day("1.6.21") {
            "9-5" - "regular shit" - biz
        }
    }
}
```

4. run the main application `TimesheetApp`

# TODO

* [x] test working on day off/weekend/holiday
* [ ] continuation time definition; e.g.: "13 coding, 14:30 meeting" (assume gapless time tracking)
* [ ] check+test for time overlap
* [ ] support simplified syntax: `year("2021") { month("June") { day("Tue 10." | "10." | "10" ) { ... } } }`
* [ ] colorized output
* [ ] detailed output (first print balance, then peer week each balance/total balance so far)

## Outlook

* [ ] overview categories time spent (pie chart)
* [ ] different Processors (text, excel)
