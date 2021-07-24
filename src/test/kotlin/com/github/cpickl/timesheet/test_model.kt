package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.OffReasons
import com.github.cpickl.timesheet.builder.Tags
import com.github.cpickl.timesheet.builder.WorkDayDsl

object AnyTag : Tag {
    override val label = "anyTag"
}
object AnyOffReason : OffReason {
    override val label = "anyOffReason"
}

val Tag.Companion.any get() = AnyTag

val OffReason.Companion.any get() = AnyOffReason

val Tags.Companion.any: Tags
    get() = object : Tags {
        override fun all() = listOf<Tag>(AnyTag)
    }

val OffReasons.Companion.any: OffReasons
    get() = object : OffReasons {
        override fun all() = listOf<OffReason>(AnyOffReason)
    }
