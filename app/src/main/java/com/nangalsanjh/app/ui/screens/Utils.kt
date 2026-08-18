package com.nangalsanjh.app.ui.screens

import java.util.Calendar

fun isWithinWorkingHours(): Boolean {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return hour in 9..18
}

fun getTimeAgo(timeStamp: Long): String {
    val diff = System.currentTimeMillis() - timeStamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}
