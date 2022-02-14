package com.miqdad.smartalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.miqdad.smartalarm.AlarmReceiver.Companion.TYPE_REPEATING
import com.miqdad.smartalarm.data.Alarm
import com.miqdad.smartalarm.data.local.AlarmDB
import com.miqdad.smartalarm.databinding.ActivityRepeatingAlarmBinding
import com.miqdad.smartalarm.fragment.TimeDialogFragment
import com.miqdad.smartalarm.helper.timeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RepeatingAlarmActivity : AppCompatActivity(), TimeDialogFragment.TimeDialogListener {

    private var _binding : ActivityRepeatingAlarmBinding? = null
    private val binding get() = _binding as ActivityRepeatingAlarmBinding
    private val db by lazy { AlarmDB(this) }

    private var alarmReceiver : AlarmReceiver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRepeatingAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmReceiver = AlarmReceiver()
        initView()
    }

    private fun initView() {
        binding.apply {
            btnSetTime.setOnClickListener{
                val timePickerDialog = TimeDialogFragment()
                timePickerDialog.show(supportFragmentManager, "TimePickerDialog")
            }
            btnAdd.setOnClickListener {
                val time = tvOnceTime.text.toString()
                val message = edtNote.text.toString()

                if (time != "Time"){
                    alarmReceiver?.setRepeating(
                        applicationContext,
                        TYPE_REPEATING,
                        time,
                        message
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        db.alarmDao().addAlarm(
                            Alarm(0,
                                "Repeating Alarm",
                                time,
                                message,
                                TYPE_REPEATING
                            )
                        )
                        finish()
                    }
                }else{
                    Toast.makeText(
                        this@RepeatingAlarmActivity,
                        "Alarm belum di tentukan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            btnCancel.setOnClickListener { finish() }
        }
    }


    override fun onTimeSetListener(tag: String?, hour: Int, minute: Int) {
        binding.tvOnceTime.text = timeFormatter(hour, minute)
    }
}