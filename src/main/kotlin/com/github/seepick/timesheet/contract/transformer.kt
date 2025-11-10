package com.github.seepick.timesheet.contract

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.timesheet.TimeEntries

fun transformContracts(contracts: List<DefinedWorkContract>, timeEntries: TimeEntries): List<RangedWorkContract> =
    contracts.mapIndexed { index, rangedContract ->
        val endDate = if((contracts.size - 1) != index) {
            contracts[index + 1].definedAt.minusDays(1)
        } else {
            timeEntries.lastDate
        }
        RangedWorkContract(rangedContract.contract, DateRange(startDate = rangedContract.definedAt, endDate = endDate))
    }
