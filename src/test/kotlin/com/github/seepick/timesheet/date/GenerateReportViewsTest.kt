package com.github.seepick.timesheet.date

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class GenerateReportViewsTest : StringSpec({
    "When start after end Then fail" {
        shouldThrow<IllegalArgumentException> {
            generateViews("10.2.25", "1.2.25")
        }
    }
    "When single day Then only monthly view" {
        generateViews("1.2.25", "1.2.25").shouldBeSingleton().first() shouldBe monthView(2025, 2)
    }
    "When multiple days same month Then only monthly view" {
        generateViews("1.2.25", "5.2.25").shouldBeSingleton().first() shouldBe monthView(2025, 2)
    }
    "When over two months Then 2 monthly and 1 yearly view" {
        generateViews("1.2.25", "30.3.25").shouldHaveSize(3).shouldContainAll(
            monthView(2025, 2), monthView(2025, 3), yearView(2025)
        )
    }
    "When over two years Then 2 monthly and 2 yearly and a total view" {
        generateViews("25.12.25", "3.1.26").shouldHaveSize(5).shouldContainAll(
            monthView(2025, 12), yearView(2025),
            monthView(2026, 1), yearView(2026),
            totalView("1.12.25", "31.1.26")
        )
    }
})
