package com.zainabshumaila.taskmanagerapp.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zainabshumaila.taskmanagerapp.R
import com.zainabshumaila.taskmanagerapp.view.fragements.PieFragment

class PieChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_chart)
        // Load your PieFragment into this activity
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PieFragment())
            .commit()

// Optional: enable back (Up) button if you have an ActionBar/Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Productivity"

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PieFragment())
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}