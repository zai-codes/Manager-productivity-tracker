package com.zainabshumaila.taskmanagerapp.view.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.zainabshumaila.taskmanagerapp.R
import com.zainabshumaila.taskmanagerapp.worker.ReminderWorker
import com.zainabshumaila.taskmanagerapp.view.activities.SignUpActivity
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // 🔔 Request notification permission (Android 13+)
    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Notifications disabled ❌", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Check and request notification permission
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

// 🔔 Ask for notification permission
        ensureNotificationPermission()

// 🔥 Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

// 🔔 Create notification channel (needed for ReminderWorker)
        createReminderChannel()

// 🎨 UI references
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val darkModeSwitch = findViewById<Switch>(R.id.darkModeSwitch)

// 🔑 Login button logic
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful ✅", Toast.LENGTH_SHORT).show()

// Get logged-in user ID
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

// 📅 Schedule ReminderWorker for deadlines
                        scheduleReminderWorker(userId)

// Go to TasksActivity
                        startActivity(Intent(this, TasksActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

// 📝 Signup button logic
        signupButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

// 🌙 Dark Mode toggle
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

// 🚫 Hide system bars (optional)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    // 🔔 Create notification channel (required for Android 8+)
    private fun createReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // 📅 Schedule ReminderWorker (runs every 24h)
    private fun scheduleReminderWorker(userId: String) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInputData(workDataOf("uid" to userId))
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "task_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}