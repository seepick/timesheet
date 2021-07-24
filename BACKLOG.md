
# Now

* [1] gradle run java app main class 
* [1] logger
* [5] DSL feature:  day("tue 10" )
* [5] allow day off day ranges: `dayOff(13 to 18)` or `dayOff("28.1.", "5.2")` or `dayOff(28, Month.July, 3, Month.August")`

# Later

* [5] overview categories time spent (pie chart? balken diagram... see notes)
* [3] weekly report: first print balance, then peer week each balance/total balance so far
* [8] auto push messages
    * every day; via notification popup;
    * A) create kts kotlin script; execute via cronjob; defines timesheet and invokes kscript dependency (publish to maven local)
    * B) create assembly and run in background; some evaluate kt file (dynamically load intellij's generated class file?! ;))
* [3] use arrow's either for builder validation
* [5] different Processors (text, excel)

## Multi User

* [3] Configurable hours per day (currently hardcoded 8)
