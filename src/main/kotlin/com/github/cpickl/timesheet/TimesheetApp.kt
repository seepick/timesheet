package com.github.cpickl.timesheet

object TimesheetApp {
    @JvmStatic
    fun main(args: Array<String>) {
        println("timesheet app running ...")
        println()
        val sheetFinder = ClasspathTimesheetFinder("com.github.cpickl.timesheet.MyTimeSheet")
        val sheet = sheetFinder.find()
        sheet.printCli()
    }
}
