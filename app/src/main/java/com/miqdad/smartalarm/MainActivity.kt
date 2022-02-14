package com.miqdad.smartalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.miqdad.smartalarm.adapter.AlarmAdapter
import com.miqdad.smartalarm.data.Alarm
import com.miqdad.smartalarm.data.local.AlarmDB
import com.miqdad.smartalarm.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding as ActivityMainBinding
    private var alarmAdapter: AlarmAdapter? = null
    private val db by lazy { AlarmDB(this) }
    private var alarmService : AlarmReceiver? = null

    override fun onResume() {
        super.onResume()

        db.alarmDao().getAlarm().observe(this){
            alarmAdapter?.setData(it)
            Log.i("GetAlarm", "setupReclerView: with this data $it")
        }

//        CoroutineScope(Dispatchers.IO).launch {
//            val alarm = db.alarmDao().getAlarm()
//            withContext(Dispatchers.Main) {
//                alarmAdapter?.setData(alarm)
//            }
//            Log.i("GetAlarm", "setupReclerView: with this data $alarm")
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//
//        initTimeToday()
//        initDateToday()
        alarmService = AlarmReceiver()
        initView()

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.apply {
            alarmAdapter = AlarmAdapter()
            rvReminderAlarm.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = alarmAdapter
            }
            swipeToDelete(rvReminderAlarm)
        }
    }

    private fun initView() {
        binding.apply {
            cvSetOneTimeAlarm.setOnClickListener {
                startActivity(Intent(this@MainActivity, OneTimeAlarmActivity::class.java))
            }
            cvSetRepeatingAlarm.setOnClickListener {
                startActivity(Intent(this@MainActivity, RepeatingAlarmActivity::class.java))
            }
        }
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = alarmAdapter?.listAlarm?.get(viewHolder.adapterPosition)
                CoroutineScope(Dispatchers.IO).launch {
                    deletedItem?.let { db.alarmDao().deleteAlarm(it) }
                    Log.i("DeleteAlarm", "onSwiped: Succes deleted alarm with $deletedItem")
                }
                deletedItem?.type?.let { alarmService?.cancelAlarm(applicationContext, it) }
//                alarmAdapter?.notifyItemRemoved(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(recyclerView)
    }


    // di nonaktifkan karena sudah memakai text clock di xml agar lebih ringkas
//      private fun initDateToday() {
//          val calendar = Calendar.getInstance()
//          val dateFormat = SimpleDateFormat("E, dd, MMMM, yyy", Locale.getDefault())
//          val formattedDate = dateFormat.format(calendar.time)
//
//          binding.tvDateToday.text = formattedDate
//      }
//
//      private fun initTimeToday() {
//          //caledar untuk mendapatkan segala hal dengan waktu dan calendar
//          // dan dibuat variabel untuk mengambil waktu d
//          val calendar = Calendar.getInstance()
//          //menentukan format jam yang akan digunakan, contohnya 13.444 atau 01:44 p.m atau
//          val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//          val formattedTime = timeFormat.format(calendar.time)
//
//          binding.tvTimeToday.text = formattedTime
//      }
}