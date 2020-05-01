package com.hraa.worldweather.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.util.Log
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.JobIntentService.enqueueWork
import androidx.core.content.ContextCompat.getSystemService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("TAG", "onReceive called, context is $context")

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent?.action) || "com.hraa.worldweather.alarm.alerted".equals(
                intent?.action
            )
        ) {
            Log.e("TAG", intent?.action.toString())
            val serviceIntent = initialServiceIntent(context)
            Log.e("TAG", "ServiceIntent is ${serviceIntent} and context is ${context}")
            NotificationService().enqueueWork(context!!, serviceIntent)
        }
//        } else {
//            Log.e("TAG", "onReceive starting the service...")
//            val serviceIntent = initialServiceIntent(context, city, units)
//            context?.startService(serviceIntent)
//        }
    }

    private fun initialServiceIntent(context: Context?): Intent {
        return Intent(context, NotificationService::class.java)
    }
}