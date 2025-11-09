package com.github.seepick.timesheet

import java.time.LocalDate

fun transformContracts(contracts: List<DslWorkContract>, timeEntries: TimeEntries): List<RangedWorkContract> =
    contracts.mapIndexed { index, rangedContract ->
        val endDate = if((contracts.size - 1) != index) {
            contracts[index + 1].definedAt.minusDays(1)
        } else {
            timeEntries.lastDate
        }
        RangedWorkContract(rangedContract.contract, DateRange(startDate = rangedContract.definedAt, endDate = endDate))
    }

data class WorkContract(
    val daysOff: Set<WorkDay>,
    val hoursPerWeek: Int,
) {
    companion object {
        val default = WorkContract(emptySet(), 38)
    }
    val hoursPerDay = hoursPerWeek / (WorkDay.entries.size - daysOff.size).toDouble()
}

data class RangedWorkContract(
    val contract: WorkContract,
    val dateRange: DateRange,
)

data class DslWorkContract(
    val contract: WorkContract,
    val definedAt: LocalDate,
)
