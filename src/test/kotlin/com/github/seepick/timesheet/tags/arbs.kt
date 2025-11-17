package com.github.seepick.timesheet.tags

import com.github.seepick.timesheet.test_infra.TestConstants
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

open class TestableTag(
    override val label: String
) : Tag

data object AnyTag : TestableTag("anyTag")
data object Tag1 : TestableTag("tag1")
data object Tag2 : TestableTag("tag2")

val TestConstants.tag1 get() = Tag1

val Tag.Companion.any get() = AnyTag
val Tag.Companion.tag1 get() = Tag1
val Tag.Companion.tag2 get() = Tag2

val Tags.Companion.any: Tags
    get() = object : Tags {
        override fun all() = listOf<Tag>(AnyTag)
    }

fun Arb.Companion.tag(): Arb<Tag> = arbitrary {
    object : Tag {
        override val label = string().next()
    }
}
