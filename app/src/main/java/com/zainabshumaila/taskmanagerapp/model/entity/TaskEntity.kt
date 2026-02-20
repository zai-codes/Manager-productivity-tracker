package com.zainabshumaila.taskmanagerapp.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

// 🔐 User account linkage
    val uid: String, // email/Google Sign-In user ID

// 📝 Task details
    val title: String,
    val description: String? = null,

// 🎯 Priority (High, Medium, Low)
    val priority: String = "Medium",

// ⏰ Deadlines & reminders
    val deadline: Long? = null, // timestamp in millis
    val reminderTime: Long? = null, // optional reminder

// ✅ Completion tracking
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,

// ⏳ Time tracking
    val startTime: Long? = null,
    val endTime: Long? = null,

// ☁️ Sync & backup metadata
    val synced: Boolean = false, // true if synced to cloud
    val lastModified: Long = System.currentTimeMillis()
)