package com.tokko.brewlog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.kodein.di.direct
import org.kodein.di.generic.instance
import java.util.*

class AlarmReciever : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { context ->
            intent?.let { intent ->
                val kodein = (context as BrewLogApplication).kodein
                val firestoreRepository: IFirestoreRepository = kodein.direct.instance()
                val alarmId = intent.getStringExtra("alarmId")
                alarmId?.let {
                    firestoreRepository.getAlarm(it) { alarm ->

                        val notificationManager: NotificationManager = kodein.direct.instance()
                        notificationManager.createNotificationChannel(
                            NotificationChannel(
                                "brews",
                                "brews",
                                NotificationManager.IMPORTANCE_DEFAULT
                            )
                        )
                        val contentIntent = Intent(context, MainActivity::class.java).apply {
                            putExtra(
                                "brewId",
                                alarm.brewId
                            )
                        }
                        val contentPendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            contentIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        val deleteIntent = Intent(context, DismissReceiver::class.java).apply {
                            putExtra(
                                "alarmId",
                                alarm.id
                            )
                        }
                        val deletePendingIntent = PendingIntent.getBroadcast(
                            context,
                            0,
                            deleteIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        val notification = Notification.Builder(context, "brews")
                            .setContentTitle(alarm.headline)
                            .setContentText(alarm.message)
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