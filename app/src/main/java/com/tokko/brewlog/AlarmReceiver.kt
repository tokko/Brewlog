package com.tokko.brewlog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { c ->
            intent?.let { intent ->
                val koin = (c.applicationContext as BrewLogApplication).getKoin()
                val fireStoreRepository: IFirestoreRepository = koin.get()
                val alarmId = intent.getStringExtra("alarmId")
                alarmId?.let {
                    fireStoreRepository.getAlarm(it) { alarm ->
                        if (!alarm.validate()) return@getAlarm
                        val notificationManager: NotificationManager = koin.get()
                        notificationManager.createNotificationChannel(
                            NotificationChannel(
                                "brews",
                                "brews",
                                NotificationManager.IMPORTANCE_DEFAULT
                            )
                        )
                        val contentIntent = Intent(c, MainActivity::class.java).apply {
                            putExtra(
                                "brewId",
                                alarm.brewId
                            )
                        }
                        val flags =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
                        val contentPendingIntent = PendingIntent.getActivity(
                            c,
                            0,
                            contentIntent,
                            flags
                        )
                        val deleteIntent = Intent(c, DismissReceiver::class.java).apply {
                            action = alarmId
                            putExtra(
                                "alarmId",
                                alarm.id
                            )
                        }
                        val deletePendingIntent = PendingIntent.getBroadcast(
                            c,
                            0,
                            deleteIntent,
                            flags
                        )
                        val notification = Notification.Builder(c, "brews")
                            .setContentTitle(alarm.headline)
                            .setContentText(alarm.message)
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setAutoCancel(false)
                            .setContentIntent(contentPendingIntent)
                            .setDeleteIntent(deletePendingIntent)
                            .build()
                        notificationManager.notify(Random().nextInt(), notification)
                    }

                }

            }
        }
    }

}