package com.github.seepick.timesheet.tags

interface Tag {
    val label: String

    companion object;
}

data class NamedTag(
    override val label: String
) : Tag

interface Tags {
    fun all(): List<Tag>
    fun contains(tag: Tag): Boolean = all().contains(tag)

    companion object
}
