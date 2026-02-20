package com.zainabshumaila.taskmanagerapp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zainabshumaila.taskmanagerapp.R
import com.zainabshumaila.taskmanagerapp.model.entity.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onCheckChanged: (TaskEntity, Boolean) -> Unit
) : ListAdapter<TaskEntity, TaskAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDeadline: TextView = itemView.findViewById(R.id.tvDeadline)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)

        holder.tvTitle.text = task.title


// ✅ Format and show deadline if available
        if (task.deadline != null) {
            val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
            holder.tvDeadline.text = sdf.format(Date(task.deadline))
        } else {
            holder.tvDeadline.text = "No deadline"
        }

// ✅ Handle checkbo

    }

    class DiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem == newItem
        }
    }
}