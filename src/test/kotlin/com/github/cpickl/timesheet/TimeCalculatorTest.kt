package com.github.cpickl.timesheet

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

class TimeCalculatorTest : StringSpec() {
    init {
        val clock = mockk<Clock>()
        val now = LocalDate.of(2021, 6, 1)
        every { clock.currentLocalDate() } returns now
        val calc = TimeCalculator(clock)

        "foo" {
            val sheet = timesheet {
                day("1.6.21") {
                    "10-16" about "any"
                }
            }

            val report = calc.foo(sheet)
            println(report)
        }

    }
}