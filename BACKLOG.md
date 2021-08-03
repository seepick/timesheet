
# Now

* [1] gradle run java app main class 
* [2] support CLI args (for cronjob): `/.../.gradlew -PreportType=cliSimple` (cliSimple, cliLong, macNotify, macSay)
* [1] cronjob for autopush
* [1] logger
* [2] daily report (today's/yesterday's balance)
* [5] DSL feature: `day("tue 10")` (?)
* [5] allow day off day ranges: `dayOff(13 to 18)` or `dayOff("28.1.", "5.2")` or `dayOff(28, Month.July, 3, Month.August")`

# Later
* [5] overview categories time spent (pie chart? balken diagram... see notes)
* [3] weekly report: first print balance, then peer week each balance/total balance so far
* [3] use arrow's either for builder validation
* [5] different Processors (text, excel)

## Future

* [3] Configurable hours per day (currently hardcoded 8)
* [99] macOs UI tool, super fast type / "fire" entries (like search bar addition)
* [99] nerd like CLI (like git commit)
* [99] pomodore timer
* [50] integration tasks + pomodore
* [99] integration azure devops, get assigned tasks (save URL)
