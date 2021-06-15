package com.github.cpickl.timesheet

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun timesheet(initCode: TimeSheetInitDsl.() -> Unit = {}, entryCode: TimeSheetDsl.() -> Unit): TimeSheet {
    val dsl = DslImplementation()
    dsl.initCode()
    dsl.entryCode()
    return dsl.build()
}

class InvalidTimesheetModelException(message: String) : Exception(message)

interface TimeSheetInitDsl {
    var daysOff: MutableSet<WorkDay>
}

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy")

private fun String.parseDate() = LocalDate.parse(this, DATE_FORMAT)

private fun String.parseTime(): Pair<LocalTime, LocalTime> {
    val parts = split("-")
    return parseTimePart(parts[0]) to parseTimePart(parts[1])
}

private fun parseTimePart(part: String) = if (part.contains(":")) {
    val hourMinute = part.split(":")
    LocalTime.of(hourMinute[0].toInt(), hourMinute[1].toInt())
} else {
    LocalTime.of(part.toInt(), 0)
}


class DslImplementation : TimeSheetInitDsl, TimeSheetDsl, DayDsl, DayOffDsl, PostAboutDsl {

    override var daysOff = mutableSetOf<WorkDay>()

    private val entries = mutableListOf<IntermediateEntry>()
    private lateinit var currentDay: LocalDate
    private lateinit var currentEntry: IntermediateEntry

    override fun day(date: String, code: DayDsl.() -> Unit) {
        currentDay = date.parseDate()
        this.code()
    }

    override fun dayOff(date: String): DayOffDsl {
        currentDay = date.parseDate()
        currentEntry = IntermediateDayOffEntry(currentDay)
        entries += currentEntry
        return this
    }

    override infix fun String.about(description: String): PostAboutDsl {
        currentEntry = IntermediateWorkDayEntry(currentDay, this.parseTime(), description)
        entries += currentEntry
        return this@DslImplementation
    }

    override operator fun String.minus(description: String) = about(description)

    override fun tag(tag: IntermediateTag) {
        val entry = currentEntry as IntermediateWorkDayEntry
        entry.tag = tag
    }

    override operator fun minus(tag: IntermediateTag) = tag(tag)

    override fun DayOffDsl.becauseOf(reason: DayOffReason) {
        val entry = currentEntry as IntermediateDayOffEntry
        entry.reason = reason
    }

    fun build(): TimeSheet {
        validate()
        return TimeSheet(
            daysOff = daysOff,
            entries = TimeEntries(entries.map { it.toRealEntry() })
        )
    }

    private fun validate() {
        if (entries.isEmpty()) throw InvalidTimesheetModelException("No entries given")
        if (entries.first() !is IntermediateWorkDayEntry) throw InvalidTimesheetModelException("First entry must be a work day")
    }

    private fun IntermediateEntry.toRealEntry(): TimeEntry {
        return when (this) {
            is IntermediateWorkDayEntry -> WorkDayEntry(
                hours = EntryDateRange(
                    day = currentDay,
                    range = TimeRange(
                        start = timeRange.first,
                        end = timeRange.second,
                    )
                ),
                description = description,
                tag = tag.realTag,
            )
            is IntermediateDayOffEntry -> DayOffEntry(
                day = currentDay,
                tag = this.reason?.realTag
                    ?: throw InvalidTimesheetModelException("no day off reason was given for: $this")
            )
        }
    }
}

private sealed class IntermediateEntry

private data class IntermediateWorkDayEntry(
    val day: LocalDate,
    val timeRange: Pair<LocalTime, LocalTime>,
    val description: String,
) : IntermediateEntry() {
    var tag: IntermediateTag = IntermediateTag.none
}

private data class IntermediateDayOffEntry(
    val day: LocalDate,
) : IntermediateEntry() {
    var reason: DayOffReason? = null
}

interface TimeSheetDsl {
    fun day(date: String, code: DayDsl.() -> Unit)
    fun dayOff(date: String): DayOffDsl

    infix fun DayOffDsl.becauseOf(reason: DayOffReason)
}

enum class IntermediateTag(val realTag: Tag) {
    none(Tag.None),
    biz(Tag.Business),
    orga(Tag.Organization),
    meet(Tag.Meeting),
    code(Tag.Coding),
    edu(Tag.Education),
    ;

    companion object
}

interface DayOffDsl {
}

enum class DayOffReason(val realTag: OffTag) {
    Sickness(OffTag.Sick),
    PublicHoliday(OffTag.PublicHoliday),
    Vacation(OffTag.Vacation);

    companion object
}

interface DayDsl {
    infix fun String.about(description: String): PostAboutDsl
    operator fun String.minus(description: String): PostAboutDsl

}

interface PostAboutDsl {
    infix fun tag(tag: IntermediateTag)
    operator fun minus(tag: IntermediateTag)
}
