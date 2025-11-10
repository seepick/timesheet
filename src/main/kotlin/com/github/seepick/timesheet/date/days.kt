package com.github.seepick.timesheet.date

import java.time.DayOfWeek

@Suppress("ClassName")
sealed interface Day {
    data object saturday : Day {
        override val javaDay: DayOfWeek = DayOfWeek.SATURDAY
    }

    data object sunday : Day {
        override val javaDay: DayOfWeek = DayOfWeek.SUNDAY
    }

    companion object {
        val all: List<Day> = listOf(
            WorkDay.monday, WorkDay.tuesday, WorkDay.wednesday, WorkDay.thursday, WorkDay.friday, saturday, sunday
        )
    }

    val javaDay: DayOfWeek
}

@Suppress("ClassName")
sealed interface WorkDay : Day {
    data object monday : WorkDay {
        override val javaDay: DayOfWeek = DayOfWeek.MONDAY
    }

    data object tuesday : WorkDay {
        override val javaDay: DayOfWeek = DayOfWeek.TUESDAY
    }

    data object wednesday : WorkDay {
        override val javaDay: DayOfWeek = DayOfWeek.WEDNESDAY
    }

    data object thursday : WorkDay {
        override val javaDay: DayOfWeek = DayOfWeek.THURSDAY
    }

    data object friday : WorkDay {
        override val javaDay: DayOfWeek = DayOfWeek.FRIDAY
    }

    companion object {
        val all: List<WorkDay> = listOf(
            monday, tuesday, wednesday, thursday, friday
        )
    }
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
