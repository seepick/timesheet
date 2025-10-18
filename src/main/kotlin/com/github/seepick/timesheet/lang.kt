@file:JvmName("Lang")

package com.github.seepick.timesheet

import java.time.DayOfWeek

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
