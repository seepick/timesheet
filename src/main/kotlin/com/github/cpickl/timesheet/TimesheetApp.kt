package com.github.cpickl.timesheet

object TimesheetApp {
    @JvmStatic
    fun main(args: Array<String>) {
        println("timesheet app running ...")
        val finder = ClasspathTimesheetFinder("com.github.cpickl.timesheet.MyTimeSheet")
        val sheet = finder.find()
        val report = TimeCalculator().foo(sheet)
        println(report)
    }
}
