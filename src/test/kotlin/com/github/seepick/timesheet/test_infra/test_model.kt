package com.github.seepick.timesheet.test_infra

import com.github.seepick.timesheet.contract.WorkContract
import com.github.seepick.timesheet.date.TimeRange
import com.github.seepick.timesheet.date.WorkDay
import com.github.seepick.timesheet.off.OffReasons
import com.github.seepick.timesheet.tags.Tag
import com.github.seepick.timesheet.tags.Tags
import com.github.seepick.timesheet.timesheet.OffReason
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
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
        daysOff = workDays().next(),
        hoursPerWeek = int(min = 10, max = 50).next()
    )
}

fun Arb.Companion.workDays() = arbitrary {
    val amount = int(min = 0, max = 3).next()
    WorkDay.all.shuffled().take(amount).toSet()
}

fun Arb.Companion.timeRange() = arbitrary {
    val hour = int(min = 0, max = 22).next()
    TimeRange(
        start = LocalTime.of(hour, 0),
        end = LocalTime.of(hour + 1, 0),
    )
}

