package com.github.seepick.timesheet.tags

/** handy function to improve readability (avoid generic usage of `listOf`) */
fun tags(tag1: Tag, vararg moreTags: Tag): List<Tag> =
    mutableListOf(tag1) + moreTags
