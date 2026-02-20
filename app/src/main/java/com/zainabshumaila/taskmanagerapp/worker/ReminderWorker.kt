package com.zainabshumaila.taskmanagerapp.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zainabshumaila.taskmanagerapp.R
import com.zainabshumaila.taskmanagerapp.model.database.AppDatabase
import java.util.Date

class ReminderWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uid = inputData.getString("uid") ?: return Result.failure()

// Next 24 hours window
        val now = System.currentTimeMillis()
        val next24h = now + 24L * 60 * 60 * 1000

// Room DAO
        val dao = AppDatabase.Companion.getInstance(appContext).taskDao()

// IMPORTANT: use a suspend DAO method returning List to avoid Flow collection in Worker
        val upcoming = dao.getTasksWithDeadlinesOnce(uid, now, next24h)

// Notify for each upcoming task
        for (task in upcoming) {
            val deadlineText = formatDeadline(task.deadline)
            showNotification(
                id = task.id.toInt(),
                title = "Upcoming deadline",
                text = "${task.title} • Due by $deadlineText"
            )
        }

        return Result.success()
    }

    private fun showNotification(id: Int, title: String, text: String) {
// Android 13+ runtime notification permission check
        if (Build.VERSION.SDK_INT >= 33 &&
            appContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
// Permission not granted: skip posting to avoid SecurityException
            return
        }

        val builder = NotificationCompat.Builder(appContext, "reminder_channel")
            .setSmallIcon(R.drawable.ic_notification) // ensure this resource exists
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(appContext).notify(id, builder.build())
    }

    private fun formatDeadline(millis: Long?): String {
        return millis?.let { Date(it).toString() } ?: "No deadline"
    }
}