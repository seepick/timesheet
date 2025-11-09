package com.github.seepick.timesheet.date

import java.time.DayOfWeek

@Suppress("EnumEntryName")
enum class WorkDay(val javaDay: DayOfWeek, val day: Day) {
    monday(DayOfWeek.MONDAY, Day.monday),
    tuesday(DayOfWeek.TUESDAY, Day.tuesday),
    wednesday(DayOfWeek.WEDNESDAY, Day.wednesday),
    thursday(DayOfWeek.THURSDAY, Day.thursday),
    friday(DayOfWeek.FRIDAY, Day.friday),
}

@Suppress("EnumEntryName")
enum class Day(val javaDay: DayOfWeek) {
    monday(DayOfWeek.MONDAY),
    tuesday(DayOfWeek.TUESDAY),
    wednesday(DayOfWeek.WEDNESDAY),
    thursday(DayOfWeek.THURSDAY),
    friday(DayOfWeek.FRIDAY),
    saturday(DayOfWeek.SATURDAY),
    sunday(DayOfWeek.SUNDAY),
}

val DayOfWeek.isWeekDay: Boolean
    get() = when (this) {
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY -> true
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY -> false
    }
