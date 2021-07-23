package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.DayOffReasonDso
import com.github.cpickl.timesheet.builder.TagDso

val DayOffReasonDso.Companion.any get() = DayOffReasonDso.PublicHoliday
val TagDso.Companion.any get() = TagDso.meet
