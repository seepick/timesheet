
# Now

* [1] logger
* [5] DSL feature:  day("tue 10" )
* [5] allow day off day ranges: `dayOff("28.1.2021", "3.2.2021")`

# Later

* [3] detailed output (first print balance, then peer week each balance/total balance so far)
* [3] use arrow's either for builder validation
* [1] multi-user: customizable work entry tags
* [1] multi-user: customizable day off reason
* [8] auto push messages
    * every day; via notification popup;
    * A) create kts kotlin script; execute via cronjob; defines timesheet and invokes kscript dependency (publish to maven local)
    * B) create assembly and run in background; some evaluate kt file (dynamically load intellij's generated class file?! ;))
* [5] overview categories time spent (pie chart)
* [5] different Processors (text, excel)
