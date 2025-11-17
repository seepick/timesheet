package com.github.seepick.timesheet.calc

import com.github.seepick.timesheet.date.DateRange
import com.github.seepick.timesheet.date.parseDate

operator fun DateRange.Companion.invoke(dates: Pair<String, String>) =
    DateRange(dates.first.parseDate(), dates.second.parseDate())
