package com.studyhub.sa.ui.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.studyhub.sa.R
import com.studyhub.sa.data.remote.RetrofitClient
import com.studyhub.sa.data.remote.dto.CreateEventRequest
import com.studyhub.sa.data.remote.dto.EventDto
import com.studyhub.sa.ui.common.BottomNavHelper
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Screen 10: Academic Calendar Screen.
 * Real events from GET/POST /api/events, added via a genuine date/time
 * picker (no more typing a date by hand) and a test/assignment/personal
 * type choice. Events show up on the Dashboard's "Upcoming" section too
 * and generate a Notifications feed entry.
 */
class AcademicCalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_academic_calendar)

        findViewById<android.view.View>(R.id.btnAddEvent).setOnClickListener { showAddEventDialog() }

        BottomNavHelper.setup(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_calendar)

        loadEvents()
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
    }

    private fun loadEvents() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@AcademicCalendarActivity).getEvents()
                if (response.isSuccessful) render(response.body().orEmpty())
            } catch (e: Exception) {
                toast("Could not load events — check your connection")
            }
        }
    }

    private fun render(events: List<EventDto>) {
        val container = findViewById<LinearLayout>(R.id.eventsContainer)
        container.removeAllViews()

        if (events.isEmpty()) {
            val empty = TextView(this).apply {
                text = getString(R.string.no_upcoming_events)
                setTextColor(ContextCompat.getColor(this@AcademicCalendarActivity, R.color.muted))
                textSize = 13f
            }
            container.addView(empty)
            return
        }

        events.forEach { e ->
            val colorRes = when (e.type) {
                "test" -> R.color.primary
                "assignment" -> R.color.gold
                else -> R.color.green
            }
            val row = layoutInflater.inflate(R.layout.item_upcoming_event, container, false)
            row.findViewById<TextView>(R.id.tvEventTitle).text = e.title
            row.findViewById<TextView>(R.id.tvEventTime).text =
                e.event_date + (e.event_time?.let { ", $it" } ?: "")
            row.findViewById<android.view.View>(R.id.eventColorBar)
                .setBackgroundColor(ContextCompat.getColor(this, colorRes))
            row.setOnLongClickListener {
                lifecycleScope.launch {
                    try {
                        RetrofitClient.getInstance(this@AcademicCalendarActivity).deleteEvent(e.id)
                        toast("Event deleted")
                        loadEvents()
                    } catch (_: Exception) { toast("Could not delete — check your connection") }
                }
                true
            }
            container.addView(row)
        }
    }

    private fun showAddEventDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val etTitle = view.findViewById<EditText>(R.id.etEventTitle)
        val etDate = view.findViewById<EditText>(R.id.etEventDate)
        val etTime = view.findViewById<EditText>(R.id.etEventTime)
        etDate.isFocusable = false
        etTime.isFocusable = false

        var selectedType = "personal"
        val typeLabels = arrayOf("Personal", "Test", "Assignment")
        val typeValues = arrayOf("personal", "test", "assignment")

        etDate.setOnClickListener { showDatePicker(etDate) }
        etTime.setOnClickListener { showTimePicker(etTime) }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.add_event_title)
            .setView(view)
            .setSingleChoiceItems(typeLabels, 0) { _, which -> selectedType = typeValues[which] }
            .setPositiveButton(R.string.action_add_event) { _, _ ->
                val title = etTitle.text.toString().trim()
                val date = etDate.text.toString().trim()
                if (title.isEmpty() || date.isEmpty()) {
                    toast("Please add a title and pick a date")
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.getInstance(this@AcademicCalendarActivity)
                            .createEvent(CreateEventRequest(title, date, etTime.text.toString().trim().ifEmpty { null }, selectedType))
                        if (response.isSuccessful) {
                            toast("Event added")
                            loadEvents()
                        } else {
                            toast(response.errorBody()?.string() ?: "Could not add event (${response.code()})")
                        }
                    } catch (e: Exception) {
                        toast("Could not reach the server — check your connection")
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.show()
    }

    private fun showDatePicker(target: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            target.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(target: EditText) {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            target.setText(String.format("%02d:%02d", hour, minute))
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
