package com.skylarksit.module.ui.lists.hyperlocal.adapters

import org.joda.time.LocalDate

class DateTimeEntry {
    var label: String? = null
    var isActive = false
    var header: String? = null
    var timeEntries: MutableList<DateTimeEntry> = mutableListOf()
    var time: String? = null
    var date: LocalDate? = null
}
