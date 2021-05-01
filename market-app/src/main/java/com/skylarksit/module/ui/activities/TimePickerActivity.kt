package com.skylarksit.module.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import com.skylarksit.module.R
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.lists.hyperlocal.adapters.DateTimeEntry
import com.skylarksit.module.ui.lists.hyperlocal.adapters.TimePickerAdapter
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.*

class TimePickerActivity : MyAppCompatActivity() {

    lateinit var list: ListView
    private lateinit var titleLabel: TextView
    private var isDate = true
    private val timeEntries: MutableList<DateTimeEntry> = ArrayList()
    var adapter: TimePickerAdapter? = null
    private var activeService: ServiceObject? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_picker)
        list = findViewById(R.id.list)
        titleLabel = findViewById(R.id.titleLabel)
        val slug = intent.getStringExtra("activeService")
        activeService = model.findProviderByName(slug)
        if (activeService == null) return
        val schedulesBeen = activeService!!.schedule
        if (schedulesBeen != null) {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            val timeZone = DateTimeZone.forOffsetMillis(cal.timeZone.getOffset(cal.timeInMillis))
            val nowDateTime = Instant.now().toDateTime(timeZone)
            val start = LocalDate()
            val end = start.plusDays(7)
            var date = start
            while (date.isBefore(end)) {
                val dayEntry = DateTimeEntry()
                val isToday = isToday(date)
                if (isToday) {
                    dayEntry.label = getString(R.string.today)
                } else {
                    dayEntry.label = date.toString(format) + Utilities.getDayOfMonthSuffix(date.dayOfMonth)
                }
                dayEntry.date = date
                val dayOfWeek = date.dayOfWeek
                for (b in schedulesBeen) {
                    if (b.dayOfWeek == dayOfWeek) {
                        if (b.isActive) {
                            dayEntry.isActive = true
                            dayEntry.timeEntries = ArrayList()
                            val timeStart = Utilities.timeFormatJoda.parseLocalTime(b.timeOpenString)
                            val timeEnd = Utilities.timeFormatJoda.parseLocalTime(b.timeClosedString)
                            val openDateTime = timeStart.toDateTimeToday(timeZone)
                            var closeDateTime = timeEnd.toDateTimeToday(timeZone)
                            if (timeEnd.isBefore(timeStart)) {
                                closeDateTime = closeDateTime.plusDays(1)
                            }
                            var time = openDateTime
                            while (time.isBefore(closeDateTime)) {
                                var timeString = time.toString(Utilities.timeFormatJoda)
                                if (isToday && time.isBefore(
                                        nowDateTime //.plusHours(1);
                                    )
                                ) {
                                    time = time.plusMinutes(b.timeInterval)
                                    continue
                                }
                                val brick = time.plusMinutes(b.timeInterval)
                                val endTimeString = brick.toString(Utilities.timeFormatJoda)
                                timeString += " - $endTimeString"
                                val timeEntry = DateTimeEntry()
                                timeEntry.timeEntries = ArrayList()
                                timeEntry.label = timeString
                                timeEntry.time = endTimeString
                                timeEntry.isActive = true
                                timeEntry.date = date
                                dayEntry.timeEntries.add(timeEntry)
                                time = time.plusMinutes(b.timeInterval)
                            }
                        }
                        break
                    }
                }
                if (isToday && !activeService!!.isClosed()) {
                    val timeEntry = DateTimeEntry()
                    timeEntry.label = "ASAP"
                    timeEntry.isActive = true
                    dayEntry.timeEntries.add(0, timeEntry)
                }
                if (Utilities.isNullOrEmpty(dayEntry.timeEntries)) {
                    dayEntry.isActive = false
                }
                timeEntries.add(dayEntry)
                date = date.plusDays(1)
            }
            val items: MutableList<DateTimeEntry> = ArrayList()
            items.addAll(timeEntries)
            adapter = TimePickerAdapter(context, items, object : TimePickerAdapter.BtnClickListener {
                override fun onBtnClick(item: DateTimeEntry?) {
                    item?.let { processClick(it) }
                }

            })
            list.adapter = adapter
            list.divider = null
        }
    }

    private fun processClick(entry: DateTimeEntry) {
        if (!entry.isActive) {
            Utilities.Toast(getString(R.string.closed_on_that_day))
            return
        }
        if (isDate) {
            titleLabel.text = getString(R.string.what_time)
            isDate = false
            adapter!!.items.clear()
            adapter!!.items.addAll(entry.timeEntries)
            adapter!!.notifyDataSetChanged()
        } else {
            val data = Intent()
            val selectedDate: String? = if (entry.date != null) {
                entry.date!!.toString(formatDateShort) + ", " + entry.label
            } else {
                entry.label
            }
            data.putExtra("selectedDate", selectedDate)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    override fun onBackPressed() {
        if (!isDate) {
            isDate = true
            adapter!!.items.clear()
            adapter!!.items.addAll(timeEntries)
            adapter!!.notifyDataSetChanged()
            titleLabel.text = getString(R.string.choose_date)
            return
        }
        super.onBackPressed()
    }

    private fun isToday(time: LocalDate?): Boolean {
        return LocalDate.now() == LocalDate(time)
    }

    companion object {
        private val format = DateTimeFormat.forPattern("EEEE, d").withLocale(Locale.ENGLISH)
        private val formatDateShort = DateTimeFormat.forPattern("MMM d").withLocale(Locale.ENGLISH)
    }
}
