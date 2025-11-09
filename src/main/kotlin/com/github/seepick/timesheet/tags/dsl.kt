package com.github.seepick.timesheet.tags

interface TagDsl {
    infix fun tag(tag: Tag)
    fun tags(tag1: Tag, vararg moreTags: Tag)
    operator fun minus(tag: Tag)
    operator fun minus(tags: List<Tag>)
}
