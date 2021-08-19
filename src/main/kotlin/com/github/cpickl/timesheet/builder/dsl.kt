@file:JvmName("Dsl")

package com.github.cpickl.timesheet.builder

import com.github.cpickl.timesheet.OffReason
import com.github.cpickl.timesheet.Tag
import com.github.cpickl.timesheet.TimeSheet
import com.github.cpickl.timesheet.WorkDay
import java.time.Month

interface Tags {
    fun all(): List<Tag>
    fun contains(tag: Tag): Boolean = all().contains(tag)

    companion object
}

interface OffReasons {
    fun all(): List<OffReason>
    fun contains(tag: OffReason): Boolean = all().contains(tag)

    companion object
}

internal class TimeSheetContext<TAGS : Tags, OFF : OffReasons>(
    val tags: TAGS,
    val offs: OFF,
    val init: TimeSheetInitDsl.() -> Unit = {}
)

fun <TAGS : Tags, OFF : OffReasons> timesheet(
    tags: TAGS,
    offs: OFF,
    init: TimeSheetInitDsl.() -> Unit = {},
    entryCode: TimeSheetDsl.() -> Unit
): TimeSheet {
    val context = TimeSheetContext(tags, offs, init)
    val dsl = DslImplementation(context)
    context.init(dsl)
    dsl.entryCode()
    return dsl.build()
}

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class TimesheetAppDsl

interface TimeSheetInitDsl {
    var daysOff: MutableSet<WorkDay>
}

@TimesheetAppDsl
interface TimeSheetDsl {
    @Deprecated(message = "year(2000) { month(1) { day(2) } }")
    fun day(date: String, code: WorkDayDsl.() -> Unit)

    @Deprecated(message = "year(2000) { month(1) { day(2) } }")
    fun dayOff(date: String): DayOffDsl

    fun year(year: Int, code: YearDsl.() -> Unit)

    infix fun DayOffDsl.becauseOf(reason: OffReason)
}

interface YearDsl {
    fun month(month: Month, code: YearMonthDsl.() -> Unit)
}

@TimesheetAppDsl
interface YearMonthDsl {
    fun day(day: Int, code: WorkDayDsl.() -> Unit)
    fun dayOff(day: Int): DayOffDsl
    fun daysOff(days: IntRange): DayOffDsl

    // necessary duplicate
    infix fun DayOffDsl.becauseOf(reason: OffReason)
}


@TimesheetAppDsl
interface WorkDayDsl {
    infix fun String.about(description: String): PostAboutDsl
    operator fun String.minus(description: String): PostAboutDsl
}

interface DayOffDsl {
}

interface PostAboutDsl {
    infix fun tag(tag: Tag)
    fun tags(tag1: Tag, vararg moreTags: Tag)
    operator fun minus(tag: Tag)
    operator fun minus(tags: List<Tag>)
}

// handy function to improve readability (avoid generic usage of `listOf`)
fun tags(tag1: Tag, vararg moreTags: Tag): List<Tag> =
    mutableListOf(tag1) + moreTags
