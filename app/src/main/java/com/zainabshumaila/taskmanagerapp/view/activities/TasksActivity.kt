package com.zainabshumaila.taskmanagerapp.view.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.zainabshumaila.taskmanagerapp.view.activities.PieChartActivity
import com.zainabshumaila.taskmanagerapp.R
import com.zainabshumaila.taskmanagerapp.view.adapters.TaskAdapter
import com.zainabshumaila.taskmanagerapp.model.entity.TaskEntity
import com.zainabshumaila.taskmanagerapp.viewmodel.TasksViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

// ✅ Extension to check if task is due today
fun TaskEntity.isDueToday(): Boolean {
    val deadline = this.deadline ?: return false
    val calDeadline = Calendar.getInstance().apply { timeInMillis = deadline }
    val calToday = Calendar.getInstance()
    return calDeadline.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
            calDeadline.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
}

class TasksActivity : AppCompatActivity() {

    private val vm: TasksViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    private lateinit var tvTodayCount: TextView
    private lateinit var tvScheduledCount: TextView
    private lateinit var tvAllCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        tvTodayCount = findViewById(R.id.tvTodayCount)
        tvScheduledCount = findViewById(R.id.tvScheduledCount)
        tvAllCount = findViewById(R.id.tvAllCount)

        val recycler = findViewById<RecyclerView>(R.id.rvTaskLists)
        val btnAddTask = findViewById<Button>(R.id.btnAddTask)
        val btnAddList = findViewById<Button>(R.id.btnAddList)

        adapter = TaskAdapter { task, isChecked ->
            vm.toggleCompleted(task.id, isChecked)
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        vm.tasks.observe(this) { taskList ->
            adapter.submitList(taskList)
            tvAllCount.text = taskList.size.toString()
            val todayCount = taskList.count { it.isDueToday() }
            tvTodayCount.text = todayCount.toString()
            val scheduledCount = taskList.count { it.deadline != null }
            tvScheduledCount.text = scheduledCount.toString()
        }

        lifecycleScope.launch {
            val completedCount = vm.getCompletedTaskCountForToday()
            tvTodayCount.text = completedCount.toString()
        }

        btnAddTask.setOnClickListener { showAddOrEditDialog(existing = null) }
        btnAddList.setOnClickListener {
            startActivity(Intent(this, PieChartActivity::class.java))
        }
    }

    private fun showAddOrEditDialog(existing: TaskEntity?) {
        val priorities = arrayOf("Low", "Medium", "High")
        var deadlineMillis: Long? = existing?.deadline
        var selectedPriorityIndex = priorities.indexOf(existing?.priority ?: "Medium")

        val titleInput = TextInputEditText(this).apply {
            setText(existing?.title ?: "")
        }
        val titleLayout = TextInputLayout(this).apply {
            hint = "Task title"
            isHintEnabled = true
            addView(titleInput)
        }

        val calendarButton = Button(this).apply {
            text = "📅 Set deadline"
            setOnClickListener {
                val c = Calendar.getInstance().apply {
                    timeInMillis = deadlineMillis ?: System.currentTimeMillis()
                }
                DatePickerDialog(
                    this@TasksActivity,
                    { _, y, m, d ->
                        val dc = Calendar.getInstance().apply {
                            set(y, m, d, 23, 59, 59)
                            set(Calendar.MILLISECOND, 0)
                        }
                        deadlineMillis = dc.timeInMillis
                        toast("Deadline set: $d/${m + 1}/$y")
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 12, 24, 0)
            addView(titleLayout)
            addView(calendarButton)
        }

        val builder = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "New Task" else "Edit Task")
            .setView(container)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .setSingleChoiceItems(priorities, selectedPriorityIndex) { _, which ->
                selectedPriorityIndex = which
            }

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = titleInput.text?.toString()?.trim().orEmpty()
                if (title.isEmpty()) {
                    toast("Title required")
                    return@setOnClickListener
                }

                val selectedPriorityText = priorities[selectedPriorityIndex] // ✅ String

                if (existing == null) {
                    vm.addTask(
                        title = title,
                        description = null,
                        priority = selectedPriorityText, // ✅ String
                        deadline = deadlineMillis,
                        deadlineAt = null
                    )
                    toast("Task added")
                } else {
                    vm.updateTask(
                        existing.copy(
                            title = title,
                            priority = selectedPriorityText, // ✅ String
                            deadline = deadlineMillis,
                            lastModified = System.currentTimeMillis()
                        )
                    )
                    toast("Task updated")
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}