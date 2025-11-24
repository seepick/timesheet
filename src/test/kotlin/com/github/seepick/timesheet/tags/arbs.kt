package com.github.seepick.timesheet.tags

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

fun Arb.Companion.tags(): Arb<Tags> = arbitrary {
    object : Tags {
        override fun all(): List<Tag> = listOf(tag().next())
    }
}

fun Arb.Companion.tag(): Arb<Tag> = arbitrary {
    NamedTag(string(minSize = 1, maxSize = 5, codepoints = Codepoint.az()).next())
//    object : Tag ... no, as equals/hashCode won't work
}
