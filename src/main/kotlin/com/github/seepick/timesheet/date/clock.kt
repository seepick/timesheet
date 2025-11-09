package com.github.seepick.timesheet.date

import java.time.LocalDate
import java.time.LocalDateTime

interface Clock {
    fun currentLocalDate(): LocalDate
    fun currentLocalDateTime(): LocalDateTime
}

object SystemClock : Clock {
    override fun currentLocalDate(): LocalDate = LocalDate.now()
    override fun currentLocalDateTime(): LocalDateTime = LocalDateTime.now()
}
