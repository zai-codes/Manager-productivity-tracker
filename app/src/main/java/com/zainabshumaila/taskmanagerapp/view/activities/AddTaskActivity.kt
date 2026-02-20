package com.zainabshumaila.taskmanagerapp.view.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.zainabshumaila.taskmanagerapp.R
import com.zainabshumaila.taskmanagerapp.worker.ReminderReceiver
import com.zainabshumaila.taskmanagerapp.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var viewModel: TasksViewModel
    private var selectedDeadlineMillis: Long? = null

    // Priority options aligned to TaskEntity.priority (String)
    private val priorities = listOf("Low", "Medium", "High")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        viewModel = ViewModelProvider(this)[TasksViewModel::class.java]

        val titleEt = findViewById<EditText>(R.id.editTask)
        val descriptionEt = findViewById<EditText>(R.id.editDescription)
        val prioritySpinner = findViewById<Spinner>(R.id.spinnerPriority)
        val btnPickDate = findViewById<Button>(R.id.btnPickDate)
        val tvDeadline = findViewById<TextView>(R.id.tvDeadline)
        val btnSave = findViewById<Button>(R.id.btnSaveTask)

        tvDeadline.text = getString(R.string.no_deadline)

// Set up priority spinner with text values
        prioritySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            priorities
        )

// Date + time picker for deadline
        btnPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_deadline_date))
                .build()

            picker.show(supportFragmentManager, "datePicker")
            picker.addOnPositiveButtonClickListener { selection ->
// selection is UTC midnight millis of chosen date
                val calendar = Calendar.getInstance().apply { timeInMillis = selection }

                val timePicker = TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        selectedDeadlineMillis = calendar.timeInMillis

                        val formattedDateTime = SimpleDateFormat(
                            "dd MMM yyyy hh:mm a", Locale.getDefault()
                        ).format(Date(selectedDeadlineMillis!!))

                        tvDeadline.text = getString(R.string.deadline_label, formattedDateTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                timePicker.show()
            }
        }

        btnSave.setOnClickListener {
            val title = titleEt.text.toString().trim()
            val description = descriptionEt.text.toString().trim()
            val priorityIndex = prioritySpinner.selectedItemPosition
            val priorityText = priorities.getOrElse(priorityIndex) { "Medium" }

            if (title.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_title_warning), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

// Save task using String priority and nullable deadline
            viewModel.addTask(
                title = title,
                description = if (description.isEmpty()) null else description,
                priority = priorityText,
                deadline = selectedDeadlineMillis,
                deadlineAt = System.currentTimeMillis()
            )

// Optional: refresh any counts if you display them elsewhere
            viewModel.fetchCompletedTasksForToday()

// Schedule reminder only if deadline is set
            selectedDeadlineMillis?.let { deadlineMillis ->
                val message = getString(
                    R.string.task_due_message,
                    title,
                    SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
                        .format(Date(deadlineMillis))
                )
                scheduleReminder(this, title, message, deadlineMillis)
            }

            Toast.makeText(this, getString(R.string.task_saved_message), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun scheduleReminder(context: Context, title: String, message: String, triggerAtMillis: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("task_title", title)
            putExtra("task_message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            title.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
}