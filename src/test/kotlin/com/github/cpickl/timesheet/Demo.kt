package com.github.cpickl.timesheet

import com.github.cpickl.timesheet.builder.DayDsl
import com.github.cpickl.timesheet.builder.TagDso
import com.github.cpickl.timesheet.builder.timesheet
import java.time.Month

fun main() {
    timesheet {
        year(2021) {
            month(Month.JULY) {
                day(1) {
                    "9-10" - "self admin" - TagDso.orga
                    standup() // enhance DSL with custom extension functions; enjoy the full power of code!
                    "10:30-12:30" - "commons tests" - TagDso.code
                    "13:30-14:30" - "refine stories" - TagDso.biz
                    "14:30-16" - "commons tests" - TagDso.code
                    "16-17" - "story alignment" - TagDso.meet
                }
            }
        }
    }.also {
        println(it)
    }
}

private fun DayDsl.standup() {
    "10-10:30" - "standup" - TagDso.meet
}