package com.github.seepick.timesheet

import com.github.seepick.timesheet.builder.OffReasons
import com.github.seepick.timesheet.builder.Tags

open class TestableTag(
    override val label: String
) : Tag

object AnyTag : TestableTag("anyTag")
object Tag1 : TestableTag("tag1")
object Tag2 : TestableTag("tag2")

object AnyOffReason : OffReason {
    override val label = "anyOffReason"
}

val Tag.Companion.any get() = AnyTag
val Tag.Companion.tag1 get() = Tag1
val Tag.Companion.tag2 get() = Tag2

val OffReason.Companion.any get() = AnyOffReason

val Tags.Companion.any: Tags
    get() = object : Tags {
        override fun all() = listOf<Tag>(AnyTag)
    }

val OffReasons.Companion.any: OffReasons
    get() = object : OffReasons {
        override fun all() = listOf<OffReason>(AnyOffReason)
    }
