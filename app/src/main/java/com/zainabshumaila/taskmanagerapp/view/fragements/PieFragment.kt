package com.zainabshumaila.taskmanagerapp.view.fragements

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.zainabshumaila.taskmanagerapp.R
import com.zainabshumaila.taskmanagerapp.viewmodel.TasksViewModel
import kotlinx.coroutines.launch

class PieFragment : Fragment(R.layout.fragment_pie) {

    // ✅ Using activityViewModels so it shares the same TasksViewModel as your main UI
    private val vm: TasksViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pie = view.findViewById<PieChart>(R.id.pieChart)

        configurePie(pie)

// Collect the all-time completed vs pending counts
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.completedVsPendingAll.collect { (completed, pending) ->
                    Log.d("PieDebug","completed:$completed,Pending:$pending")
                    setPieData(pie, completed, pending)
                }
            }
        }
    }

    private fun configurePie(pie: PieChart) {
        pie.description.isEnabled = false
        pie.isDrawHoleEnabled = true
        pie.setHoleColor(resolveColor(android.R.attr.colorBackground))
        pie.setUsePercentValues(true)
        pie.setEntryLabelColor(resolveColor(android.R.attr.textColorPrimary))
        pie.legend.isEnabled = false
        pie.setNoDataText("No tasks yet")
    }

    private fun setPieData(pie: PieChart, completed: Int, pending: Int) {
        val entries = mutableListOf<PieEntry>()

        if (completed > 0) entries.add(PieEntry(completed.toFloat(), "Completed"))
        if (pending > 0) entries.add(PieEntry(pending.toFloat(), "Pending"))

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No data"))
        }

        val set = PieDataSet(entries, "Task Status").apply {
            colors = listOf(
                colorState(R.color.teal_700, R.color.teal_200), // Completed
                colorState(R.color.error, R.color.error) // Pending
            )
            valueTextColor = resolveColor(android.R.attr.textColorPrimary)
            valueTextSize = 12f
            sliceSpace = 2f
        }

        val data = PieData(set).apply {
            setValueFormatter(PercentFormatter(pie))
        }

        pie.data = data
        pie.highlightValues(null)
        pie.invalidate()
    }

    private fun colorState(light: Int, dark: Int): Int {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return ContextCompat.getColor(
            requireContext(),
            if (uiMode == Configuration.UI_MODE_NIGHT_YES) dark else light
        )
    }

    private fun resolveColor(attr: Int): Int {
        val typed = TypedValue()
        requireContext().theme.resolveAttribute(attr, typed, true)
        return ContextCompat.getColor(requireContext(), typed.resourceId)
    }
}