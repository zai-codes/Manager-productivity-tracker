package com.zainabshumaila.taskmanagerapp.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zainabshumaila.taskmanagerapp.model.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // 🔹 Basic CRUD
    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id AND uid = :uid")
    suspend fun deleteById(id: Long, uid: String)

    // 🔹 Observe all tasks (ordered by completion, priority, deadline)
    @Query("""
SELECT * FROM tasks
WHERE uid = :uid
ORDER BY completed ASC,
CASE priority
WHEN 'High' THEN 3
WHEN 'Medium' THEN 2
ELSE 1 END DESC,
CASE WHEN deadline IS NULL THEN 1 ELSE 0 END,
deadline ASC
""")
    fun observeAll(uid: String): Flow<List<TaskEntity>>

    // 🔹 Get single task
    @Query("SELECT * FROM tasks WHERE id = :id AND uid = :uid")
    suspend fun getById(id: Long, uid: String): TaskEntity?

    // 🔹 Mark task completed
    @Query("UPDATE tasks SET completed = :completed, completedAt = :updatedAt WHERE id = :id AND uid = :uid")
    suspend fun setCompleted(id: Long, completed: Boolean, updatedAt: Long, uid: Long)

    // ✅ Completed tasks for a specific day
    @Query("""
SELECT * FROM tasks
WHERE completed = 1
AND uid = :uid
AND deadline BETWEEN :startOfDay AND :endOfDay
""")
    fun getCompletedTasksByDate(uid: String, startOfDay: Long, endOfDay: Long): List<TaskEntity>

    // ✅ Count of completed tasks for a specific day
    @Query("""
SELECT COUNT(*) FROM tasks
WHERE completed = 1
AND uid = :uid
AND deadline IS NOT NULL
AND deadline BETWEEN :startOfDay AND :endOfDay
""")
    suspend fun getCompletedTaskCountByDate(uid: String, startOfDay: Long, endOfDay: Long): Int

    // ✅ Count of pending tasks for a specific day
    @Query("""
SELECT COUNT(*) FROM tasks
WHERE completed = 0
AND uid = :uid
AND createdAt BETWEEN :startOfDay AND :endOfDay
""")
    suspend fun getPendingTaskCountByDate(uid: String, startOfDay: Long, endOfDay: Long): Int

    // 📊 Productivity Insights (Pie chart data)
    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 1 AND uid = :uid")
    fun completedCountAll(uid: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 0 AND uid = :uid")
    fun pendingCountAll(uid: String): Flow<Int>

    @Query("""
SELECT COUNT(*) FROM tasks
WHERE completed = 1
AND uid = :uid
AND deadline BETWEEN :start AND :end
""")
    fun completedCountInRange(uid: String, start: Long, end: Long): Flow<Int>

    @Query("""
SELECT COUNT(*) FROM tasks
WHERE completed = 0
AND uid = :uid
AND deadline BETWEEN :start AND :end
""")
    fun pendingCountInRange(uid: String, start: Long, end: Long): Flow<Int>

    // ⏳ Time tracking
    @Query("""
SELECT SUM(endTime - startTime) FROM tasks
WHERE completed = 1 AND uid = :uid
""")
    suspend fun getTotalTimeSpent(uid: String): Long

    // 🔔 Tasks with upcoming deadlines (for reminders)
    @Query("""
SELECT * FROM tasks
WHERE uid = :uid
AND deadline IS NOT NULL
AND deadline BETWEEN :start AND :end
ORDER BY deadline ASC
""")
    fun getTasksWithDeadlines(uid: String, start: Long, end: Long): Flow<List<TaskEntity>>

    // ☁️ Sync status
    @Query("SELECT * FROM tasks WHERE uid = :uid AND synced = 0")
    suspend fun getUnsyncedTasks(uid: String): List<TaskEntity>
    @Query("""
SELECT * FROM tasks
WHERE uid = :uid
AND deadline IS NOT NULL
AND deadline BETWEEN :start AND :end
ORDER BY deadline ASC
""")
    suspend fun getTasksWithDeadlinesOnce(uid: String, start: Long, end: Long): List<TaskEntity>


}