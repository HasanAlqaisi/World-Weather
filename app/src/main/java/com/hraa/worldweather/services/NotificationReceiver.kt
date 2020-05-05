package com.hraa.worldweather.services


import android.content.BroadcastReceiver
import android.util.Log
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent?.action)
            || "com.hraa.worldweather.alarm.alerted".equals(intent?.action)
        ) {
            val serviceIntent = Intent(context, NotificationService::class.java)
            NotificationService().enqueueWork(context!!, serviceIntent)
        }
    }
}