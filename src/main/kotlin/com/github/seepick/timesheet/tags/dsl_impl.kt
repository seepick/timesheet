package com.github.seepick.timesheet.tags

import com.github.seepick.timesheet.dsl.BuilderWorkDayEntry
import com.github.seepick.timesheet.dsl.Current
import com.github.seepick.timesheet.dsl.TimeSheetContext
import com.github.seepick.timesheet.off.OffReasons

class TagDslImpl<TAGS : Tags, OFFS : OffReasons>(
    private val context: TimeSheetContext<TAGS, OFFS>,
    private val current: Current
) : TagDsl {

    override fun tag(tag: Tag) {
        addTags(listOf(tag))
    }

    override fun tags(tag1: Tag, vararg moreTags: Tag) {
        addTags(listOf(tag1, *moreTags))
    }

    private fun addTags(allTags: List<Tag>) {
        val entry = current.entry as BuilderWorkDayEntry
        allTags.forEach { tag ->
            if (!context.tags.contains(tag)) {
                // TODO test me; if configure no tags, and this tag requested doesnt exist; throw!
            }
            entry.tags += tag
        }
    }

    override operator fun minus(tag: Tag) = tag(tag)

    override operator fun minus(tags: List<Tag>) = addTags(tags)
}
