package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.DayOffReasonDso
import com.github.cpickl.timesheet.builder.BuilderTag

val DayOffReasonDso.Companion.any get() = DayOffReasonDso.PublicHoliday
val BuilderTag.Companion.any get() = BuilderTag.meet
