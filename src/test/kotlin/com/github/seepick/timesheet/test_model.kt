package com.github.seepick.timesheet

import com.github.seepick.timesheet.builder.OffReasons
import com.github.seepick.timesheet.builder.Tags
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.set
import java.time.LocalTime

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


fun Arb.Companion.workContract() = arbitrary {
    WorkContract(
        daysOff = Arb.set(Arb.enum<WorkDay>(), 0..3).next(),
        hoursPerWeek = int(min = 10, max = 50).next()
    )
}

fun Arb.Companion.timeRange() = arbitrary {
    val hour = int(min = 0, max = 22).next()
    TimeRange(
        start = LocalTime.of(hour, 0),
        end = LocalTime.of(hour + 1, 0),
    )
}

