package com.github.cpickl.timesheet

object TimesheetApp {

    private val fqn = "com.github.cpickl.timesheet.MyAutoSheet"

    @JvmStatic
    fun main(args: Array<String>) {
        println("Timesheet app running ...")
        println("Searching for class at: $fqn")
        val autoTimed = loadAutoSheetFromClasspath(fqn) ?:
            throw Exception("Could not find class '$fqn' in classpath. Please create own (see README file).")
        println("Found! Fetching time sheet ...")
        val sheet = autoTimed.provide()
        println("Generating report ...")
        autoTimed.chooseReport(sheet.calculate())
    }
}
