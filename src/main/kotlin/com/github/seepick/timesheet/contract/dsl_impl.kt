package com.github.seepick.timesheet.contract

import com.github.seepick.timesheet.date.WorkDay

class ContractDslImpl : ContractDsl {

    override var hoursPerWeek = WorkContract.default.hoursPerWeek

    override var dayOff: WorkDay
        get() = if (daysOff.size == 1) daysOff.first() else throw IllegalStateException("Expected to be 1 dayOff but there were: $daysOff")
        set(value) {
            daysOff = setOf(value)
        }

    override var daysOff: Set<WorkDay> = WorkContract.default.daysOff

    override fun noDaysOff() {
        daysOff = emptySet()
    }
}
