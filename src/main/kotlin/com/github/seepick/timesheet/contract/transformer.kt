package com.github.seepick.timesheet.contract

import com.github.seepick.timesheet.date.DateRange
import java.time.LocalDate

fun transformContracts(contracts: List<DefinedWorkContract>, sheetEndDate: LocalDate): List<RangedWorkContract> =
    contracts.mapIndexed { index, rangedContract ->
        val endDate = if ((contracts.size - 1) != index) {
            contracts[index + 1].definedAt.minusDays(1)
        } else {
            sheetEndDate
        }
        RangedWorkContract(
            contract = rangedContract.contract,
            dateRange = DateRange(startDate = rangedContract.definedAt, endDate = endDate),
        )
    }
