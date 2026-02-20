package com.zainabshumaila.taskmanagerapp.model.repository

import com.zainabshumaila.taskmanagerapp.model.dao.TaskDao
import com.zainabshumaila.taskmanagerapp.model.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TaskRepository(private val dao: TaskDao) {

    fun getTasks(uid: String): Flow<List<TaskEntity>> = dao.observeAll(uid)

    suspend fun add(task: TaskEntity) {
        dao.insert(task)
    }

    suspend fun update(task: TaskEntity) {
        dao.update(task)
    }

    suspend fun remove(id: Long, uid: String) {
        dao.deleteById(id, uid)
    }

    suspend fun toggle(id: Long, completed: Boolean, uid: Long) {
        dao.setCompleted(id, completed, System.currentTimeMillis(), uid)
    }

    // ✅ Existing methods for completed tasks per day
    fun getCompletedTasksByDate(uid: String, startOfDay: Long, endOfDay: Long): List<TaskEntity> {
        return dao.getCompletedTasksByDate(uid, startOfDay, endOfDay)
    }

    suspend fun getCompletedTaskCountByDate(uid: String, startOfDay: Long, endOfDay: Long): Int {
        return dao.getCompletedTaskCountByDate(uid, startOfDay, endOfDay)
    }

// 📊 --- NEW METHODS FOR PIE CHART PRODUCTIVITY DATA ---

    /** All-time completed count for a user */
    fun completedCountAll(uid: String): Flow<Int> = dao.completedCountAll(uid)

    /** All-time pending count for a user */
    fun pendingCountAll(uid: String): Flow<Int> = dao.pendingCountAll(uid)

    /** Combined all-time completed vs pending counts */
    fun completedVsPendingAll(uid: String): Flow<Pair<Int, Int>> =
        combine(completedCountAll(uid), pendingCountAll(uid)) { completed, pending ->
            completed to pending
        }

    /** Completed count in a date range (for last 7 days Pie) */
    fun completedCountInRange(uid: String, start: Long, end: Long): Flow<Int> =
        dao.completedCountInRange(uid, start, end)

    /** Pending count in a date range (for last 7 days Pie) */
    fun pendingCountInRange(uid: String, start: Long, end: Long): Flow<Int> =
        dao.pendingCountInRange(uid, start, end)

    /** Combined completed vs pending counts in a date range */
    fun completedVsPendingInRange(uid: String, start: Long, end: Long): Flow<Pair<Int, Int>> =
        combine(
            completedCountInRange(uid, start, end),
            pendingCountInRange(uid, start, end)
        ) { completed, pending ->
            completed to pending
        }
}