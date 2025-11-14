package com.github.seepick.timesheet.date

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yy")
fun String.parseDate(): LocalDate = LocalDate.parse(this, DATE_FORMAT)

private val TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm")
val String.hrs get(): LocalTime = LocalTime.parse(this, TIME_FORMAT)

fun LocalDate.toParsableDate() = "$dayOfMonth.$monthValue.${year.toString().substring(2)}"

fun LocalTime.toParseableString() = toString()
