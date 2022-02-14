package com.miqdad.smartalarm

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap
import android.media.Ringtone
import android.media.RingtoneManager
import android.nfc.Tag
import android.os.Build
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.collections.ArrayList

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val type = intent?.getIntExtra(EXTRA_TYPE, -1)
        val message = intent?.getStringExtra(EXTRA_MESSAGE)

        val title = if (type == TYPE_ONE_TIME) "One Time Alarm" else "Repeating alarm"

        val notificationId = if (type == TYPE_ONE_TIME) ID_ONE_REPEATING else ID_REPEATING

        if (message != null) showAlarmNotification(context, title, message, notificationId)

    }

    private fun showAlarmNotification(
        context: Context?,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val channelID = "Channel_1"
        val channelName = "Alarm Manager channel"

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_one_time)
            .setContentTitle(title)
            .setContentText(message)
            .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            builder.setChannelId(channelID)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = builder.build()
        notificationManager.notify(notificationId, notification)
    }

    fun setOneTime(context: Context, type: Int, date: String, time: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_MESSAGE, message)
        intent.putExtra(EXTRA_TYPE, type)
        Log.e("ErrorSetOneTimeAlarm", "setOneTimeAlarm : $date $time")

        //date diterima -> 2-3-2022
        //spilit untuMenghilangkan tanda '-' 2 3 2022
        val dateArray = date.split("-").toTypedArray()
        val timeArray = time.split(":").toTypedArray()

        //date
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, converterData(dateArray)[0])
        calendar.set(Calendar.MONTH, converterData(dateArray)[1] - 1)
        calendar.set(Calendar.YEAR, converterData(dateArray)[2])
        //time
        calendar.set(Calendar.HOUR_OF_DAY, converterData(timeArray)[0])
        calendar.set(Calendar.MINUTE, timeArray[1].toInt())
        calendar.set(Calendar.SECOND, 0)

        val pendingIntent = PendingIntent.getBroadcast(context, ID_ONE_REPEATING, intent, 0)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Toast.makeText(context, "Sukses Ngeset One Time Alarm", Toast.LENGTH_SHORT).show()
        Log.i("SetAlarmNotification", "setOneTimeAlarm : Alarm Will rings on ${calendar.time}")
    }

    fun setRepeating(context: Context, type: Int, time: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_MESSAGE, message)
        intent.putExtra(EXTRA_TYPE, type)

        val timeArray = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]))
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]))
        calendar.set(Calendar.SECOND, 0)

        val pendingIntent = PendingIntent.getBroadcast(context, ID_REPEATING, intent, 0)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Toast.makeText(context, "Suksess Mengset Up Repeating Alarm", Toast.LENGTH_SHORT).show()
    }

    fun cancelAlarm(context: Context, type: Int,){
        //alarm Manager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //intent ke alarm receiver
        val intent = Intent(context, AlarmReceiver::class.java)

        //ambil requestCode /ID_ALARM Berdasar tipe alarmnya
        val requestCode = if (type == TYPE_ONE_TIME) ID_ONE_REPEATING else ID_REPEATING

        //cancel pending intent
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
        pendingIntent.cancel()

        //cancel alarmmanager
        alarmManager.cancel(pendingIntent)
        Log.i("CancelAlarm", "cancelAlarm: Success")
        if (type == TYPE_ONE_TIME){
            Toast.makeText(context, "Sukses Membatalkan ONE TIME ALARM", Toast.LENGTH_SHORT)
                .show()
        }else{
            Toast.makeText(context, "Sukse Membatalkan REPEATING ALARM", Toast.LENGTH_SHORT).show()
        }
    }

    //untuk mengubah String menjadi array
    fun converterData(array: Array<String>): List<Int> {
        return array.map {
            it.toInt()
        }
    }

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_MESSAGE = "message"

        const val TYPE_ONE_TIME = 0
        const val TYPE_REPEATING = 1

        const val ID_ONE_REPEATING = 101
        const val ID_REPEATING = 102
    }

}