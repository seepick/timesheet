package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.DayOffReasonDso
import com.github.cpickl.timesheet.builder.Tags

object AnyTag : Tag {
    override val label = "any"
}

val DayOffReasonDso.Companion.any get() = DayOffReasonDso.PublicHoliday
val Tag.Companion.any get() = AnyTag

val Tags.Companion.any: Tags
    get() = object : Tags {
        override fun all() = listOf<Tag>(AnyTag)
    }
