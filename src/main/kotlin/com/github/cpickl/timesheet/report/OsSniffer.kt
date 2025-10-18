package com.github.cpickl.timesheet.report

object OsSniffer {
    val os by lazy {
        sniff()
    }

    private fun sniff(): Os? {
        val name = System.getProperty("os.name").lowercase()
        return Os.entries.firstOrNull { name.contains(it.osNameFragment) }
    }
}

enum class Os(val osNameFragment: String) {
    Win("win"),
    Mac("mac")
}
