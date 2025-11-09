package com.github.seepick.timesheet.off

import com.github.seepick.timesheet.timesheet.OffReason

interface OffReasons {
    fun all(): List<OffReason>
    fun contains(tag: OffReason): Boolean = all().contains(tag)

    companion object
}
