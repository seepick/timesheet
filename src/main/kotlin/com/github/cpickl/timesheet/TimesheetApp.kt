package com.github.cpickl.timesheet

object TimesheetApp {
    @JvmStatic
    fun main(args: Array<String>) {
        println("timesheet app running ...")
        println()
        val finder = ClasspathTimesheetFinder("com.github.cpickl.timesheet.MyTimeSheet")
        val sheet = finder.find()
        val report = TimeCalculator().calculate(sheet)

        println("Result: ${report.hoursBalanceString}")
    }
}
