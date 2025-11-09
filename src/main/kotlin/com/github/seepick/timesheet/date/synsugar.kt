package com.github.seepick.timesheet.date

import com.github.seepick.timesheet.date.Day.friday
import com.github.seepick.timesheet.date.Day.monday
import com.github.seepick.timesheet.date.Day.saturday
import com.github.seepick.timesheet.date.Day.sunday
import com.github.seepick.timesheet.date.Day.thursday
import com.github.seepick.timesheet.date.Day.tuesday
import com.github.seepick.timesheet.date.Day.wednesday
import com.github.seepick.timesheet.dsl.MonthDsl
import com.github.seepick.timesheet.dsl.WorkDayDsl
import com.github.seepick.timesheet.dsl.YearDsl
import java.time.Month

val Int.st get() = this
val Int.nd get() = this
val Int.rd get() = this
val Int.th get() = this

fun YearDsl.january(code: MonthDsl.() -> Unit) = month(Month.JANUARY, code)
fun YearDsl.february(code: MonthDsl.() -> Unit) = month(Month.FEBRUARY, code)
fun YearDsl.march(code: MonthDsl.() -> Unit) = month(Month.MARCH, code)
fun YearDsl.april(code: MonthDsl.() -> Unit) = month(Month.APRIL, code)
fun YearDsl.may(code: MonthDsl.() -> Unit) = month(Month.MAY, code)
fun YearDsl.june(code: MonthDsl.() -> Unit) = month(Month.JUNE, code)
fun YearDsl.july(code: MonthDsl.() -> Unit) = month(Month.JULY, code)
fun YearDsl.august(code: MonthDsl.() -> Unit) = month(Month.AUGUST, code)
fun YearDsl.september(code: MonthDsl.() -> Unit) = month(Month.SEPTEMBER, code)
fun YearDsl.october(code: MonthDsl.() -> Unit) = month(Month.OCTOBER, code)
fun YearDsl.november(code: MonthDsl.() -> Unit) = month(Month.NOVEMBER, code)
fun YearDsl.december(code: MonthDsl.() -> Unit) = month(Month.DECEMBER, code)

fun MonthDsl.monday(day: Int, code: WorkDayDsl.() -> Unit) = day(monday, day, code)
fun MonthDsl.tuesday(day: Int, code: WorkDayDsl.() -> Unit) = day(tuesday, day, code)
fun MonthDsl.wednesday(day: Int, code: WorkDayDsl.() -> Unit) = day(wednesday, day, code)
fun MonthDsl.thursday(day: Int, code: WorkDayDsl.() -> Unit) = day(thursday, day, code)
fun MonthDsl.friday(day: Int, code: WorkDayDsl.() -> Unit) = day(friday, day, code)
fun MonthDsl.saturday(day: Int, code: WorkDayDsl.() -> Unit) = day(saturday, day, code)
fun MonthDsl.sunday(day: Int, code: WorkDayDsl.() -> Unit) = day(sunday, day, code)
