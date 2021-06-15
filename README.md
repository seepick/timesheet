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

# TODO

* [ ] check+test for time overlap
* [ ] support `day("Tue 10.6.21")`
* [ ] support `year("2021") { month("June") { day("Tue 10." | "10." | "10" ) { ... } } }`
* [ ] test working on day off/weekend/holiday

## Outlook

* [ ] colored output
* [ ] overview categories time spent (pie chart)
* [ ] different Processors (text, excel)
