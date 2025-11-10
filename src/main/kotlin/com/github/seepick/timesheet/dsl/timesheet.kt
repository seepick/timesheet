package com.github.seepick.timesheet.dsl

import com.github.seepick.timesheet.off.OffReasons
import com.github.seepick.timesheet.tags.Tags
import com.github.seepick.timesheet.timesheet.TimeSheet

fun <TAGS : Tags, OFF : OffReasons> timesheet(
    tags: TAGS,
    offs: OFF,
    entryCode: TimeSheetDsl.() -> Unit
): TimeSheet {
    val context = TimeSheetContext(tags, offs)
    val dsl = DslImplementation(context)
    dsl.entryCode()
    return dsl.build()
}

class TimeSheetContext<TAGS : Tags, OFF : OffReasons>(
    val tags: TAGS,
    val offs: OFF,
)
