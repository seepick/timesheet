package com.github.seepick.timesheet.date

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy")
fun String.parseDate(): LocalDate = LocalDate.parse(this, DATE_FORMAT)

fun LocalDate.toParsableDate() = "$dayOfMonth.$monthValue.${year.toString().substring(2)}"

fun LocalTime.toParseableString() = toString()
