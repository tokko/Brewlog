package com.tokko.brewlog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
                    firestoreRepository.getAlarm(it) {

                        val notificationManager: NotificationManager = kodein.direct.instance()
                        notificationManager.createNotificationChannel(
                            NotificationChannel(
                                "brews",
                                "brews",
                                NotificationManager.IMPORTANCE_DEFAULT
                            )
                        )

                        val notification = Notification.Builder(context, "brews")
                            .setContentTitle(it.headline)
                            .setContentText(it.message)
                            .setAutoCancel(false)
                            //.setContentIntent()
                            .build()
                        notificationManager.notify(Random().nextInt(), notification)
                    }

                }

            }
        }
    }

}