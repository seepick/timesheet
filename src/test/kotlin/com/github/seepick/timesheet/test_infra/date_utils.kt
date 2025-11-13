package com.github.seepick.timesheet.test_infra

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy")

fun String.parseDate(): LocalDate = LocalDate.parse(this, DATE_FORMAT)
