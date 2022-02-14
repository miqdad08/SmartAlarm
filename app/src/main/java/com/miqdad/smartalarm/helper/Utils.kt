package com.miqdad.smartalarm.helper

import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.SimpleFormatter

fun timeFormatter(hour : Int, minute: Int): String{
    val calendar = Calendar.getInstance()
    calendar.set(0, 0, 0,hour, minute)
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
}