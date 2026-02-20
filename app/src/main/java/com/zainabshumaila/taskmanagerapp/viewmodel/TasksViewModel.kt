package com.zainabshumaila.taskmanagerapp.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.zainabshumaila.taskmanagerapp.worker.ReminderReceiver
import com.zainabshumaila.taskmanagerapp.model.database.AppDatabase
import com.zainabshumaila.taskmanagerapp.model.entity.TaskEntity
import com.zainabshumaila.taskmanagerapp.model.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TasksViewModel(app: Application) : AndroidViewModel(app) {

    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val repo = TaskRepository(AppDatabase.Companion.getInstance(app).taskDao())

    // ✅ LiveData for all tasks
    val tasks = repo.getTasks(uid).asLiveData()

    // ✅ Add task
    fun addTask(
        title: String,
        description: String?,
        priority: String,
        deadline: Long?,
        deadlineAt: Long?
    ) = viewModelScope.launch {
        repo.add(
            TaskEntity(
                uid = uid,
                title = title,
                description = description,
                priority = priority,
                deadline = deadline,
                completed = false,
                createdAt = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis()
            )
        )
    }

    // ✅ Update task
    fun updateTask(task: TaskEntity) = viewModelScope.launch {
        repo.update(task.copy(lastModified = System.currentTimeMillis()))
    }

    // ✅ Delete task
    fun deleteTask(id: Long) = viewModelScope.launch {
        repo.remove(id, uid)
    }

    // ✅ Toggle completion
    fun toggleCompleted(id: Long, completed: Boolean) = viewModelScope.launch {
        repo.toggle(id, completed, System.currentTimeMillis())
    }

    // ✅ Completed tasks today
    fun getCompletedTasksForToday() =
        repo.getCompletedTasksByDate(uid, getStartOfDay(), getEndOfDay())

    suspend fun getCompletedTaskCountForToday() =
        repo.getCompletedTaskCountByDate(uid, getStartOfDay(), getEndOfDay())

    // ✅ Time helpers
    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    // ✅ Fetch completed tasks for today
    fun fetchCompletedTasksForToday() {
        val calStart = getStartOfDay()
        val calEnd = getEndOfDay()

    }

    // 📊 Pie chart support
    val completedVsPendingAll: StateFlow<Pair<Int, Int>> =
        repo.completedVsPendingAll(uid)
            .stateIn(viewModelScope, SharingStarted.Companion.Lazily, 0 to 0)

    fun completedVsPendingInRange(start: Long, end: Long): StateFlow<Pair<Int, Int>> =
        repo.completedVsPendingInRange(uid, start, end)
            .stateIn(viewModelScope, SharingStarted.Companion.Lazily, 0 to 0)

    // 🔔 Reminder scheduling
    fun scheduleReminder(context: Context, title: String, message: String, triggerAtMillis: Long) {
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

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun now(): Long = System.currentTimeMillis()
}