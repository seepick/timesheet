package com.github.seepick.timesheet.off

interface OffReasons {
    fun all(): List<OffReason>
    fun contains(tag: OffReason): Boolean = all().contains(tag)

    companion object
}
