package com.<your>.<application>
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.bumptech.glide.Glide
class RemindersMain01 : AppCompatActivity() {
	private var editTextValue1: String = ""
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_reminders_main01)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/pivo7nxjQM/8x1n9fi5_expires_30_days.png").into(findViewById(R.id.r8z6ow7xy3vg))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/pivo7nxjQM/zibcw8bq_expires_30_days.png").into(findViewById(R.id.rynbuuuggei))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/pivo7nxjQM/0ledvmex_expires_30_days.png").into(findViewById(R.id.rn9iviiio8v))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/pivo7nxjQM/j2mpekm4_expires_30_days.png").into(findViewById(R.id.rmks1gvlrr7e))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/pivo7nxjQM/o5mvglui_expires_30_days.png").into(findViewById(R.id.rtwygq3azj7c))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/pivo7nxjQM/ctwvmfi7_expires_30_days.png").into(findViewById(R.id.re1jf2fy924u))
		val editText1: EditText = findViewById(R.id.rzt5yap200tj)
		editText1.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
				// before Text Changed
			}
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
				editTextValue1 = s.toString()  // on Text Changed
			}
			override fun afterTextChanged(s: Editable?) {
				// after Text Changed
			}
		})
		val button1: View = findViewById(R.id.rew9tiyh3e6m)
		button1.setOnClickListener {
			println("Pressed")
		}
		val button2: View = findViewById(R.id.rmu992571vut)
		button2.setOnClickListener {
			println("Pressed")
		}
		val button3: View = findViewById(R.id.rjpjvextsjm)
		button3.setOnClickListener {
			println("Pressed")
		}
	}
}