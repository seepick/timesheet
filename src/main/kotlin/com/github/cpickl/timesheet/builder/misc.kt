@file:JvmName("Misc")

package com.github.cpickl.timesheet.builder

import com.github.cpickl.timesheet.OffTag
import com.github.cpickl.timesheet.Tag
import java.time.LocalDate

/** Construction of model because of invalid DSL definition failed. */
class BuilderException(message: String, cause: Exception? = null) : Exception(message, cause)

interface IntermediateEntryDsoFields {
    val day: LocalDate
}

internal sealed class IntermediateEntryDso : IntermediateEntryDsoFields

internal data class IntermediateWorkDayEntryDso(
    override val day: LocalDate,
    val timeRangeSpec: TimeRangeSpec,
    val about: String,
) : IntermediateEntryDso() {
    init {
        if (about.isBlank()) throw BuilderException("An entry's about text must not be blank for entry ${day.toParsableDate()}!")
    }

    var tag: TagDso = TagDso.none

    override fun toString() = "Day[${day.toParsableDate()}/${timeRangeSpec.toParseableString()} - tag: $tag]"
}

internal data class DayOffEntryDso(
    override val day: LocalDate,
) : IntermediateEntryDso() {
    var reason: DayOffReasonDso? = null

    override fun toString() = "DayOff[${day.toParsableDate()} - reason: $reason]"
}

enum class TagDso(val realTag: Tag) {
    none(Tag.None),
    biz(Tag.Business),
    orga(Tag.Organization),
    meet(Tag.Meeting),
    code(Tag.Coding),
    edu(Tag.Education),
    scrum(Tag.Scrum),
    ;

    companion object
}


enum class DayOffReasonDso(val realTag: OffTag) {
    Sickness(OffTag.Sick),
    PublicHoliday(OffTag.PublicHoliday),
    Vacation(OffTag.Vacation);

    companion object
}
