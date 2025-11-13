package com.github.seepick.timesheet.off

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

object AnyOffReason : OffReason {
    override val label = "anyOffReason"
}

val OffReason.Companion.any get() = AnyOffReason

val OffReasons.Companion.any: OffReasons
    get() = object : OffReasons {
        override fun all() = listOf<OffReason>(AnyOffReason)
    }

fun Arb.Companion.offReason(): Arb<OffReason> = arbitrary {
    object : OffReason {
        override val label = string().next()
    }
}
